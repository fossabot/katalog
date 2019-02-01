package com.bol.katalog.plugin.hazelcast

import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import com.bol.katalog.testing.TestData
import com.hazelcast.core.HazelcastInstance
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Clock
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class HazelcastAutoConfigurationTest {
    @Test
    fun canFormCluster() {
        withClusterOfSize(3) { hazelcasts ->
            expectThat(hazelcasts[0].cluster.members.size).isEqualTo(3)
        }
    }

    private fun withClusterOfSize(
        clusterSize: Int,
        block: (List<HazelcastInstance>) -> Unit
    ) {
        val contextRunner = ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HazelcastAutoConfiguration::class.java))

        // Used to wait for the block() call to be completed
        val blockDone = CompletableDeferred<Unit>()

        // When starting up, count down until we have started all the Hazelcasts
        val hazelcastsLeftToStart = CountDownLatch(clusterSize)

        // The list of Hazelcasts we have started
        val hazelcasts = CopyOnWriteArrayList<HazelcastInstance>()

        // Generate the comma-separated member list
        val members = (1..clusterSize).joinToString(",") { "localhost:${5700 + it}" }

        val threads = (1..clusterSize).map {
            thread {
                contextRunner
                    .withPropertyValues(
                        "katalog.clustering.type=hazelcast",
                        "katalog.clustering.hazelcast.instance-name=member-$it",
                        "katalog.clustering.hazelcast.port=${5700 + it}",
                        "katalog.clustering.hazelcast.members=$members"
                    )
                    .withUserConfiguration(ExtraConfiguration::class.java)
                    .run { ctx ->
                        // Start context and launch Hazelcast
                        val hazelcast: HazelcastInstance = ctx.getBean(HazelcastInstance::class)
                        hazelcasts.add(hazelcast)

                        // Countdown the latch
                        hazelcastsLeftToStart.countDown()

                        // Once everything is counted down the test block will run, we'll wait for it to finish
                        runBlocking {
                            blockDone.await()
                        }

                        // Shutdown
                        hazelcast.shutdown()
                    }
            }
        }

        hazelcastsLeftToStart.await()
        block(hazelcasts)
        blockDone.complete(Unit)

        threads.forEach { it.join() }
    }

    @Configuration
    class ExtraConfiguration {
        @Bean
        fun clock(): Clock = TestData.clock

        @Bean
        fun eventStore(): EventStore = InMemoryEventStore()
    }
}