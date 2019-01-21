package com.bol.katalog.config

import com.bol.katalog.cqrs.AggregateManager
import com.bol.katalog.security.userdirectory.UserDirectorySynchronizer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@Profile("!test") // automatically start outside of tests
class ApplicationLifecycleManager(
    private val aggregateManager: AggregateManager,
    private val synchronizer: UserDirectorySynchronizer
) {
    @PostConstruct
    fun postConstruct() {
        // After startup perform a synchronization of the user directory
        aggregateManager.addStartupListener {
            synchronizer.synchronize()
        }
        aggregateManager.start()
    }

    @PreDestroy
    fun preDestroy() {
        aggregateManager.stop()
    }
}