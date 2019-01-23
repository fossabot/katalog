package com.bol.katalog.plugin.atomix

import com.bol.katalog.cqrs.Command
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import kotlin.reflect.full.createType

class AtomixAggregateContextTest {
    private val handlerType = MyHandler::class.createType()

    @Test
    fun `Commands are always routed to leader`() {
        TestCluster().run {
            add("member-1", "member-2", "member-3")

            // Register a command handler on the leader
            onLeader {
                context.onCommand(handlerType) {
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

            // Check that we have indeed received a command from every node
            onLeader {
                expectThat(this.context.handledCommands).hasSize(3)
            }

            // ...and that the followers received nothing
            onAllFollowers {
                expectThat(this.context.handledCommands).isEmpty()
            }
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

    interface MyHandler
    class TestCommand(val value: Int) : Command
    object TestFailure : Command.Failure
}
