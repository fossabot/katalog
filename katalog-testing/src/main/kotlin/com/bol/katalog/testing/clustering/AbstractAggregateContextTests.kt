package com.bol.katalog.testing.clustering

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.reflect.full.createType

abstract class AbstractAggregateContextTests<CLUSTER_NODE : Any, CLUSTER_CONTEXT : AggregateContext> {
    private val handlerType = MyHandler::class.createType()

    fun commandsAreAlwaysRoutedToLeader(cluster: AbstractTestCluster<CLUSTER_NODE, CLUSTER_CONTEXT>) {
        val receivedCommandsOnNode = mutableSetOf<String>()

        cluster.run {
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

    fun canHandleCommandFailures(cluster: AbstractTestCluster<CLUSTER_NODE, CLUSTER_CONTEXT>) {
        cluster.run {
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