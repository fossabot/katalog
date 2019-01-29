package com.bol.katalog.plugin.atomix

import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import io.atomix.core.Atomix
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

class AtomixAutoConfigurationTest {
    @Test
    fun canFormPrimaryBackupCluster() {
        withClusterOfSize(3, AtomixProperties.AtomixProtocol.PRIMARY_BACKUP) { atomixen ->
            expectThat(atomixen[0].membershipService.reachableMembers.size).isEqualTo(3)
        }
    }

    @Test
    fun canFormRaftCluster() {
        withClusterOfSize(3, AtomixProperties.AtomixProtocol.RAFT) { atomixen ->
            expectThat(atomixen[0].membershipService.reachableMembers.size).isEqualTo(3)
        }
    }

    private fun withClusterOfSize(
        clusterSize: Int,
        protocol: AtomixProperties.AtomixProtocol,
        block: (List<Atomix>) -> Unit
    ) {
        val contextRunner = ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AtomixAutoConfiguration::class.java))

        // Used to wait for the block() call to be completed
        val blockDone = CompletableDeferred<Unit>()

        // When starting up, count down until we have started all the Atomixen
        val atomixenLeftToStart = CountDownLatch(clusterSize)

        // The list of atomixen we have started
        val atomixen = CopyOnWriteArrayList<Atomix>()

        // Generate the comma-separated member list
        val members = (1..clusterSize).joinToString(",") { "member-$it" }

        val threads = (1..clusterSize).map {
            thread {
                contextRunner
                    .withPropertyValues(
                        "katalog.clustering.type=atomix",
                        "katalog.clustering.atomix.cluster-size=$clusterSize",
                        "katalog.clustering.atomix.member-id=member-$it",
                        "katalog.clustering.atomix.members=$members",
                        "katalog.clustering.atomix.protocol=${protocol.name.toUpperCase()}"
                    )
                    .withUserConfiguration(ExtraConfiguration::class.java)
                    .run { ctx ->
                        // Start context and launch Atomix
                        val atomix: Atomix = ctx.getBean(Atomix::class)
                        atomix.start().join()
                        atomixen.add(atomix)

                        // Countdown the latch
                        atomixenLeftToStart.countDown()

                        // Once everything is counted down the test block will run, we'll wait for it to finish
                        runBlocking {
                            blockDone.await()
                        }

                        // Shutdown Atomix
                        atomix.stop().join()
                    }
            }
        }

        atomixenLeftToStart.await()
        block(atomixen)
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