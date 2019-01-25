package com.bol.katalog.plugin.atomix

import com.bol.katalog.cqrs.Command
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.full.createType

class AtomixAggregateContextTest {
    private val handlerType = MyHandler::class.createType()

    @Test
    fun `Commands are always routed to leader`() {
        val receivedCommandsOnNode = mutableSetOf<String>()

        TestCluster().run {
            add("member-1", "member-2", "member-3")

            // Register a command handler on the leader
            onLeader {
                val memberId = this.memberId
                context.onCommand(handlerType) {
                    receivedCommandsOnNode.add(memberId)
                    val command = it as TestCommand
                    expectThat(command.value).isEqualTo(123)
                    Command.Success
                }
            }

            // Send a message from all node, these should all arrive at the leader only
            onAllNodes {
                val result = context.send(handlerType, TestCommand(123))
                expectThat(result).isA<Command.Success>()
            }

            // Check that we have indeed received a command from every node on the leader only
            expectThat(receivedCommandsOnNode).containsExactly(getLeaderId())
        }
    }

    @Test
    fun `Can handle command failures`() {
        TestCluster().run {
            add("member-1")

            // Register a command handler on the leader
            onLeader {
                context.onCommand(handlerType) {
                    TestFailure
                }
            }

            // Send a message
            onLeader {
                val result = context.send(handlerType, TestCommand(123))
                expectThat(result).isA<TestFailure>()
            }
        }
    }

    @Test
    fun `Can run code on cluster startup`() {
        TestCluster().run {
            add("member-1", "member-2", "member-3")

            val counter = AtomicInteger(0)

            onAllNodes {
                context.onStartup {
                    counter.incrementAndGet()
                }

                context.invokeStartupBlocks()
            }

            expectThat(counter.get()).isEqualTo(1)
        }
    }

    interface MyHandler
    class TestCommand(val value: Int) : Command
    object TestFailure : Command.Failure
}
