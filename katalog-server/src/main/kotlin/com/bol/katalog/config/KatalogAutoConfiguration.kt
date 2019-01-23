package com.bol.katalog.config

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.StandaloneAggregateContext
import com.bol.katalog.messaging.MessageBus
import com.bol.katalog.messaging.inmemory.InMemoryMessageBus
import com.bol.katalog.security.userdirectory.UserDirectorySynchronizer
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.time.Clock
import javax.annotation.PostConstruct

@Configuration
@EnableReactiveMethodSecurity
@EnableScheduling
@EnableSpringWebSession
@EnableWebFlux
@EnableWebFluxSecurity
class KatalogAutoConfiguration : WebFluxConfigurer {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Bean
    @ConditionalOnMissingBean
    fun eventStore(): EventStore = InMemoryEventStore()

    @Bean
    @ConditionalOnMissingBean
    fun blobStore(): BlobStore = InMemoryBlobStore()

    @Bean
    @ConditionalOnMissingBean
    fun messageBus(): MessageBus = InMemoryMessageBus()

    @Bean
    @ConditionalOnMissingBean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    @ConditionalOnMissingBean
    fun aggregateContext(): AggregateContext = StandaloneAggregateContext(eventStore(), clock())

    @PostConstruct
    fun init() {
        runBlocking {
            aggregateContext().onStartup {
                val synchronizer = applicationContext.getBean<UserDirectorySynchronizer>()
                synchronizer.synchronize()
            }
        }
    }
}