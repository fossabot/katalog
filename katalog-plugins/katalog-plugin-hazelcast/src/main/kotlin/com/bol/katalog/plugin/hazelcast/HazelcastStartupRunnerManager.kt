package com.bol.katalog.plugin.hazelcast

import com.bol.katalog.config.StartupRunner
import com.bol.katalog.config.StartupRunnerManager
import com.hazelcast.core.HazelcastInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener

class HazelcastStartupRunnerManager(
    private val hazelcast: HazelcastInstance,
    private val startupRunners: List<StartupRunner>
) : StartupRunnerManager, ApplicationListener<ApplicationStartedEvent> {
    private val log = KotlinLogging.logger {}
    private var hasCompleted: Boolean = false

    override fun onApplicationEvent(event: ApplicationStartedEvent) {
        invokeStartupRunners()
    }

    override fun hasCompleted() = hasCompleted

    fun invokeStartupRunners() {
        val startupCompleted = hazelcast.getAtomicLong("startup-completed")

        try {
            if (startupCompleted.get() != 1L) {
                if (hazelcast.cluster.members.first() == hazelcast.cluster.localMember) {
                    log.info("Member '${hazelcast.name}' is running startup code")
                    for (runner in startupRunners) {
                        runner.runAfterStartup()
                    }
                    log.info("Member '${hazelcast.name}' has completed running startup code")
                    startupCompleted.set(1L)
                } else {
                    // Wait for startup to complete on other nodes
                    while (startupCompleted.get() != 1L) {
                        log.info("Member '${hazelcast.name}' is waiting for startup code to complete on leader")
                        runBlocking { delay(5000) }
                    }
                }
            }

            log.info("Startup completed")
        } finally {
            hasCompleted = true
        }
    }
}