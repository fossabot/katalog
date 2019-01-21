package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.clustering.ClusteringContext
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class AggregateManager(
    private val aggregates: List<Aggregate<*>> = emptyList(),
    private val eventStore: EventStore,
    private val clusteringContext: ClusteringContext
) {
    private var started = false

    private var startupListeners = mutableListOf<suspend () -> Unit>()

    fun addStartupListener(listener: suspend () -> Unit) {
        startupListeners.add(listener)
    }

    fun start() {
        if (started) {
            throw IllegalStateException("AggregateManager cannot be started when it is already started")
        }

        clusteringContext.start()

        aggregates.forEach {
            it.setClusteringContext(clusteringContext)
            it.start()
        }

        clusteringContext.ifRequiresReplay {
            replayFromStore()
        }

        started = true

        runBlocking {
            startupListeners.forEach { it.invoke() }
        }
    }

    fun stop() {
        if (!started) {
            throw IllegalStateException("AggregateManager cannot be stopped when it is already stopped")
        }

        aggregates.forEach {
            it.stop()
        }

        clusteringContext.stop()

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