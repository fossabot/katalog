package com.bol.blueprint.cqrs

import com.bol.blueprint.CurrentUserSupplier
import com.bol.blueprint.cqrs.api.EventHandler
import com.bol.blueprint.domain.Event
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class EventPublisher(
    private val eventStore: EventStore,
    private val userDetailsSupplier: CurrentUserSupplier,
    private val eventHandlers: List<EventHandler>,
    private val clock: Clock
) {
    init {
        runBlocking {
            replayFromStore()
        }
    }

    suspend fun publish(eventData: Any) {
        val userDetails = userDetailsSupplier.getCurrentUser()
        val event = Event(
            metadata = Event.Metadata(
                timestamp = clock.instant(),
                username = userDetails?.username ?: "unknown"
            ), data = eventData
        )
        eventStore.store(event)
        publishToListeners(event)
    }

    suspend fun replayFromStore() {
        var done = false
        var eventQuery = EventQuery()
        while (!done) {
            val page = eventStore.get(eventQuery)
            for (event in page.data) {
                publishToListeners(event)
            }

            eventQuery = page.toEventQuery()
            done = page.data.isEmpty()
        }
    }

    suspend fun publishToListeners(event: Event<Any>) {
        val completions = eventHandlers
            .map {
                val msg = EventHandler.CompletedEvent(
                    context = EventHandler.CompletedEventContext(event.metadata),
                    event = event.data,
                    completed = CompletableDeferred()
                )

                it.eventHandler.send(msg)

                msg.completed
            }

        completions.awaitAll()
    }

    fun reset() {
        eventHandlers.forEach {
            if (it is Resettable) {
                it.reset()
            }
        }
    }
}