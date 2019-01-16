package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.time.Clock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class AggregateManager(
    private val aggregates: List<Aggregate<*>> = emptyList(),
    private val eventStore: EventStore,
    private val clock: Clock
) {
    private var started = false

    @PostConstruct
    fun start() {
        if (started) {
            return
        }

        aggregates.forEach {
            it.reset()
            it.start()
            it.setEventListener(
                object : EventListener {
                    override suspend fun beforeEventProcessed(event: Event): PersistentEvent.Metadata {
                        return persistEvent(event)
                    }
                }
            )
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
                it.setEventListener(null)
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

    private suspend fun persistEvent(event: Event): PersistentEvent.Metadata {
        val userDetails = CoroutineUserContext.get()
        val persistentEvent = PersistentEvent(
            metadata = PersistentEvent.Metadata(
                timestamp = clock.instant(),
                username = userDetails?.username ?: "unknown"
            ), data = event
        )

        eventStore.store(persistentEvent)

        return persistentEvent.metadata
    }
}