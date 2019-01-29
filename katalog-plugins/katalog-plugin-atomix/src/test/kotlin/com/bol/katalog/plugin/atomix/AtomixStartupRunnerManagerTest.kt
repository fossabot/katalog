package com.bol.katalog.plugin.atomix

import com.bol.katalog.config.StartupRunner
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.atomic.AtomicInteger

class AtomixStartupRunnerManagerTest {
    @Test
    fun `Can run code on startup`() {
        val counter = AtomicInteger(0)

        val runner = object : StartupRunner {
            override fun runAfterStartup() {
                counter.incrementAndGet()
            }
        }

        TestCluster("member-1", "member-2").run {
            // Only one node should increase the counter
            onAllMembers {
                val manager = AtomixStartupRunnerManager(this.atomix, listOf(runner))
                manager.invokeStartupRunners()
            }

            expectThat(counter.get()).isEqualTo(1)
            //

            // Even after running the startup manager again on the leader, the counter should remain the same
            onLeader {
                val manager = AtomixStartupRunnerManager(this.atomix, listOf(runner))
                manager.invokeStartupRunners()
            }

            expectThat(counter.get()).isEqualTo(1)
            //

            // Even after running the startup manager again on the follower, the counter should remain the same
            onMember("member-2") {
                val manager = AtomixStartupRunnerManager(this.atomix, listOf(runner))
                manager.invokeStartupRunners()
            }

            expectThat(counter.get()).isEqualTo(1)
            //
        }
    }
}