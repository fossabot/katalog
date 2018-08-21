package com.bol.blueprint

import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.Sink
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class TestApplication {
    @Bean
    fun eventStore() = InMemoryEventStore()

    @Bean
    fun blobStore() = InMemoryBlobStore()

    @Bean
    fun commandHandler(listeners: List<Sink<Event>>) = SynchronousCommandHandler(eventStore(), blobStore(), listeners)
}