package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.hazelcast.support.HazelcastTestCluster
import com.bol.katalog.security.SystemUser
import com.bol.katalog.security.withUserId
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HazelcastAggregateContextTest {
    @Test
    fun `Commands are always routed to leader`() {
        var receivedCommands = 0

        class TestClusterAggregate(context: HazelcastAggregateContext) : HazelcastAggregate(context) {
            init {
                setup {
                    command<TestCommand> {
                        receivedCommands++
                    }
                }
            }
        }

        val aggregates = mutableMapOf<HazelcastAggregateContext, TestClusterAggregate>()

        HazelcastTestCluster(3).run {
            onAllMembers {
                aggregates[context] = TestClusterAggregate(context)
            }

            // Send a message from all nodes, these should all arrive at the leader
            onAllMembers {
                withUserId(SystemUser.get().id) {
                    context.send(TestCommand(123))
                }
            }

            // Check that we have indeed received a command from every node on the leader only
            expectThat(receivedCommands).isEqualTo(3)
        }
    }

    data class TestCommand(val value: Int) : Command
}