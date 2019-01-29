package com.bol.katalog.config

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.StandaloneAggregateContext
import com.bol.katalog.messaging.MessageBus
import com.bol.katalog.messaging.inmemory.InMemoryMessageBus
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.time.Clock

@Configuration
@EnableReactiveMethodSecurity
@EnableScheduling
@EnableSpringWebSession
@EnableWebFlux
@EnableWebFluxSecurity
class KatalogAutoConfiguration : WebFluxConfigurer {
    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Configuration
    @ConditionalOnProperty("katalog.event-store.type", havingValue = "in-memory", matchIfMissing = true)
    class InMemoryEventStoreConfiguration {
        @Bean
        fun eventStore(): EventStore = InMemoryEventStore()
    }

    @Configuration
    @ConditionalOnProperty("katalog.blob-store.type", havingValue = "in-memory", matchIfMissing = true)
    class InMemoryBlobStoreConfiguration {
        @Bean
        fun blobStore(): BlobStore = InMemoryBlobStore()
    }

    @Configuration
    @ConditionalOnProperty("katalog.message-bus.type", havingValue = "in-memory", matchIfMissing = true)
    class InMemoryMessageBusConfiguration {
        @Bean
        fun messageBus(): MessageBus = InMemoryMessageBus()
    }

    @Configuration
    @ConditionalOnProperty("katalog.clustering.type", havingValue = "standalone", matchIfMissing = true)
    class StandaloneClusteringConfiguration {
        @Bean
        fun aggregateContext(eventStore: EventStore, clock: Clock): AggregateContext =
            StandaloneAggregateContext(eventStore, clock)

        @Bean
        fun startupRunner(runners: List<StartupRunner>): StartupRunnerManager = StandaloneStartupRunnerManager(runners)
    }
}
