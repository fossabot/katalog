package com.bol.katalog.config

import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener

class StandaloneStartupRunnerManager(
    private val startupRunners: List<StartupRunner>
) : StartupRunnerManager, ApplicationListener<ApplicationStartedEvent> {
    private val log = KotlinLogging.logger {}
    private var hasCompleted: Boolean = false

    override fun onApplicationEvent(event: ApplicationStartedEvent) {
        log.info("Running startup code")
        startupRunners.forEach { it.runAfterStartup() }
        log.info("Completed running startup code")
        hasCompleted = true
    }

    override fun hasCompleted() = hasCompleted
}