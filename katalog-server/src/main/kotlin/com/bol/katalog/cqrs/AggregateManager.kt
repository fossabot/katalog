package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.clustering.ClusteringContext
import com.bol.katalog.cqrs.clustering.ClusteringContextFactory
import com.bol.katalog.cqrs.clustering.IAggregate
import com.bol.katalog.cqrs.clustering.asPersistentEvent
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class AggregateManager(
    private val aggregates: List<Aggregate<*>> = emptyList(),
    private val eventStore: EventStore,
    private val clock: Clock,
    private val clusteringContextFactory: ClusteringContextFactory
) {
    private var started = false

    fun start(): AutoCloseable {
        if (started) {
            throw IllegalStateException("AggregateManager cannot be started when it is already started")
        }

        val clusteringContext = clusteringContextFactory.get(aggregates)

        aggregates.forEach {
            it.setClusteringContext(clusteringContext)
            it.setEventPersister(eventPersister())
            it.start()
        }

        clusteringContext.ifRequiresReplay {
            replayFromStore()
        }

        started = true

        return AutoCloseable { this@AggregateManager.close(clusteringContext) }
    }

    private fun close(clusteringContext: ClusteringContext) {
        if (!started) {
            throw IllegalStateException("AggregateManager cannot be stopped when it is already stopped")
        }

        aggregates.forEach {
            it.stop()
        }

        clusteringContext.close()

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

    fun eventPersister(): suspend (Event) -> PersistentEvent<Event> {
        return {
            val persistentEvent = it.asPersistentEvent(CoroutineUserContext.get(), clock)
            eventStore.store(persistentEvent)
            persistentEvent
        }
    }
}
