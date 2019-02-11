package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.config.HazelcastStartupRunnerManager
import com.bol.katalog.config.StartupRunner
import com.bol.katalog.cqrs.hazelcast.support.HazelcastTestCluster
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.atomic.AtomicInteger

class HazelcastStartupRunnerManagerTest {
    @Test
    fun `Can run code on startup`() {
        val counter = AtomicInteger(0)

        val runner = object : StartupRunner {
            override fun runAfterStartup() {
                counter.incrementAndGet()
            }
        }

        HazelcastTestCluster(2).run {
            // Only one node should increase the counter
            onAllMembers {
                val manager = HazelcastStartupRunnerManager(this.clusterNode, listOf(runner))
                manager.invokeStartupRunners()
            }

            expectThat(counter.get()).isEqualTo(1)
            //

            // Even after running the startup manager again on the leader, the counter should remain the same
            onLeader {
                val manager = HazelcastStartupRunnerManager(this.clusterNode, listOf(runner))
                manager.invokeStartupRunners()
            }

            expectThat(counter.get()).isEqualTo(1)
            //

            // Even after running the startup manager again on the follower, the counter should remain the same
            onSingleFollower {
                val manager = HazelcastStartupRunnerManager(this.clusterNode, listOf(runner))
                manager.invokeStartupRunners()
            }

            expectThat(counter.get()).isEqualTo(1)
            //
        }
    }
}