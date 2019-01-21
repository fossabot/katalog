package com.bol.katalog.cqrs

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@Profile("!test") // automatically start outside of tests
class AggregateManagerLifecycleManager(private val aggregateManager: AggregateManager) {
    @PostConstruct
    fun postConstruct() {
        aggregateManager.start()
    }

    @PreDestroy
    fun preDestroy() {
        aggregateManager.stop()
    }
}