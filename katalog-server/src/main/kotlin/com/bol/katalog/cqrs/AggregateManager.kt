package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.clustering.ClusteringContext
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class AggregateManager(
    private val aggregates: List<Aggregate<*>> = emptyList(),
    private val eventStore: EventStore,
    private val clusteringContext: ClusteringContext
) {
    private var started = false

    @PostConstruct
    fun start() {
        if (started) {
            return
        }

        aggregates.forEach {
            it.setClusteringContext(clusteringContext)
            it.start()
        }

        runBlocking {
            replayFromStore()
        }

        started = true
    }

    @PreDestroy
    fun stop() {
        if (!started) {
            return
        }

        val jobs = aggregates.map {
            GlobalScope.launch {
                it.stop()
            }
        }

        runBlocking {
            jobs.joinAll()
        }

        started = false
    }

    suspend fun replayFromStore() {
        var done = false
        var eventQuery = EventQuery()
        while (!done) {
            val page = eventStore.get(eventQuery)

            val jobs = aggregates.map { agg ->
                GlobalScope.launch {
                    for (event in page.data) {
                        agg.handlePersistentEvent(event)
                    }
                }
            }
            jobs.joinAll()

            eventQuery = page.toEventQuery()
            done = page.data.isEmpty()
        }
    }
}