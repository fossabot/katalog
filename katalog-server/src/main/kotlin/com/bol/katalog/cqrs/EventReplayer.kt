package com.bol.katalog.cqrs

import com.bol.katalog.config.StartupRunner
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
@Order(-100) // Make sure we replay events before we start synchronizing users and generating test-data etc
class EventReplayer(
    private val eventStore: EventStore,
    private val aggregates: List<Aggregate<*>>
) : StartupRunner {
    private val log = KotlinLogging.logger {}

    override fun runAfterStartup() {
        replay()
    }

    fun replay() {
        log.info("Replaying events from event store")

        var query = EventQuery()
        var totalEvents = 0
        val time = measureTimeMillis {
            runBlocking {
                while (true) {
                    val page = eventStore.get(query)

                    aggregates.forEach { agg ->
                        page.data.forEach { event ->
                            agg.directAccess().send(event.data, event.metadata)
                        }
                    }
                    totalEvents += page.data.size

                    if (page.nextPageCursor != null) {
                        query = page.toNextPageQuery()
                    } else {
                        break
                    }
                }
            }
        }

        val timePerEvent = time.toFloat() / totalEvents
        val eventsPerSecond = 1000.0f / timePerEvent
        log.info("Replaying events sent $totalEvents events ($eventsPerSecond events per second)")
    }
}