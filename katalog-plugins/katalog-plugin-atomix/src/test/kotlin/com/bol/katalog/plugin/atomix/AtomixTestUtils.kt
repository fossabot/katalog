package com.bol.katalog.plugin.atomix

import io.atomix.core.Atomix
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun withClusterOfSize(
    clusterSize: Int,
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

    val threads = (1..clusterSize).map {
        thread {
            contextRunner
                .withPropertyValues(
                    "katalog.clustering.atomix.cluster-size=$clusterSize",
                    "katalog.clustering.atomix.member-id=member-$it"
                )
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
