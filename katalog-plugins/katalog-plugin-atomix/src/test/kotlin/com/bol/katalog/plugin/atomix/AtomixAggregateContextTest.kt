package com.bol.katalog.plugin.atomix

import com.bol.katalog.cqrs.Command
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.reflect.full.createType

class AtomixAggregateContextTest {
    private val handlerType = MyHandler::class.createType()

    @Test
    fun `Commands are always routed to leader`() {
        val receivedCommandsOnNode = mutableSetOf<String>()

        TestCluster("member-1", "member-2", "member-3").run {
            // Register a command handler on the leader
            onLeader {
                val memberId = this.memberId
                context.onCommand(handlerType) { command, metadata ->
                    receivedCommandsOnNode.add(memberId)
                    expectThat((command as TestCommand).value).isEqualTo(123)
                    expectThat(metadata.userId).isEqualTo("userId")
                    Command.Result.Success
                }
            }

            // Send a message from all node, these should all arrive at the leader only
            onAllMembers {
                val result = context.send(handlerType, TestCommand(123), Command.Metadata("userId"))
                expectThat(result).isA<Command.Result.Success>()
            }

            // Check that we have indeed received a command from every node on the leader only
            expectThat(receivedCommandsOnNode).containsExactly(getLeaderId())
        }
    }

    @Test
    fun `Can handle command failures`() {
        TestCluster("member-1").run {
            // Register a command handler on the leader
            onLeader {
                context.onCommand(handlerType) { _, _ ->
                    TestFailure
                }
            }

            // Send a message
            onLeader {
                val result = context.send(handlerType, TestCommand(123), Command.Metadata("userId"))
                expectThat(result).isA<TestFailure>()
            }
        }
    }

    interface MyHandler
    class TestCommand(val value: Int) : Command
    object TestFailure : Command.Result.Failure("Test Failure")
}
