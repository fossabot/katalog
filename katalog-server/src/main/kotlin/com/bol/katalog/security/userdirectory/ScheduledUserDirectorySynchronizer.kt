package com.bol.katalog.security.userdirectory

import com.bol.katalog.security.config.SecurityConfigurationProperties
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class ScheduledUserDirectorySynchronizer(
    private val synchronizer: UserDirectorySynchronizer,
    private val taskScheduler: TaskScheduler,
    private val config: SecurityConfigurationProperties
) {
    @PostConstruct
    fun initSynchronization() {
        val trigger =
            CronTrigger(config.userDirectories.sync.cron, TimeZone.getTimeZone(config.userDirectories.sync.timezone))
        taskScheduler.schedule(Runnable { synchronizer.synchronize() }, trigger)
    }
}