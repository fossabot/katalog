package com.bol.blueprint.config

import com.bol.blueprint.domain.Dispatcher
import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.Sink
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DispatcherConfiguration {
    @Bean
    fun query() = Query()

    @Bean
    @ConditionalOnMissingBean
    fun dispatcher(eventStore: EventStore, blobStore: BlobStore, listeners: List<Sink<Event>>) = Dispatcher(eventStore, blobStore, listeners)
}