package com.bol.katalog.cqrs2

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class AggregateStarter(private val aggregates: List<Aggregate<*>> = emptyList()) {
    @PostConstruct
    fun start() {
        val jobs = aggregates.map {
            it.start()
            GlobalScope.launch {
                it.replayFromStore()
            }
        }

        runBlocking {
            jobs.joinAll()
        }
    }

    @PreDestroy
    fun stop() {
        val jobs = aggregates.map {
            GlobalScope.launch { it.stop() }
        }

        runBlocking {
            jobs.joinAll()
        }
    }
}