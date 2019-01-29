package com.bol.katalog.plugin.atomix

import com.bol.katalog.config.StartupRunner
import com.bol.katalog.config.StartupRunnerManager
import io.atomix.core.Atomix
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import java.util.concurrent.CountDownLatch

class AtomixStartupRunnerManager(
    private val atomix: Atomix,
    private val startupRunners: List<StartupRunner>
) : StartupRunnerManager, ApplicationListener<ApplicationStartedEvent> {
    private val log = KotlinLogging.logger {}
    private var hasCompleted: Boolean = false

    override fun onApplicationEvent(event: ApplicationStartedEvent) {
        invokeStartupRunners()
    }

    override fun hasCompleted() = hasCompleted

    fun invokeStartupRunners() {
        val startupCompleted = atomix.atomicValueBuilder<Boolean>("startup-completed").build()

        try {
            if (startupCompleted.get() != true) {
                if (atomix.isLeader()) {
                    log.info("Member '${atomix.membershipService.localMember.id().id()}' is running startup code")
                    for (runner in startupRunners) {
                        runner.runAfterStartup()
                    }
                    log.info("Member '${atomix.membershipService.localMember.id().id()}' has completed running startup code")
                    startupCompleted.set(true)
                } else {
                    val latch = CountDownLatch(1)
                    startupCompleted.addListener {
                        if (it.newValue() == true) {
                            latch.countDown()
                        }
                    }

                    // Wait for startup to complete on other nodes
                    if (startupCompleted.get() != true) {
                        log.info("Member '${atomix.membershipService.localMember.id().id()}' is waiting for startup code to complete on member '${atomix.getLeaderId()}'")
                        latch.await()
                    }
                }
            }

            log.info("Startup completed")
        } finally {
            startupCompleted.close()
            hasCompleted = true
        }
    }
}