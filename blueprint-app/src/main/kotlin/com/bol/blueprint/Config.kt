package com.bol.blueprint

import com.bol.blueprint.config.fallback
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@EnableConfigurationProperties(BlueprintConfigurationProperties::class)
class Config {
    @Bean
    @Primary
    fun eventStoreFactory(beanFactory: ListableBeanFactory): FactoryBean<EventStore> = fallback(beanFactory) { InMemoryEventStore() }

    @Bean
    @Primary
    fun blobStoreFactory(beanFactory: ListableBeanFactory): FactoryBean<BlobStore> = fallback(beanFactory) { InMemoryBlobStore() }
}