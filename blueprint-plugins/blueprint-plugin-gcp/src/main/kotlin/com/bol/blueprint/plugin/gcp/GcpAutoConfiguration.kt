package com.bol.blueprint.plugin.gcp

import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore
import com.google.cloud.storage.Storage
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GcpProperties::class)
@ConditionalOnClass(Storage::class)
class GcpAutoConfiguration {
    @Bean
    fun gcpEventStore(): EventStore {
        return GcpEventStore()
    }

    @Bean
    fun gcpBlobStore(storage: Storage, gcpProperties: GcpProperties): BlobStore {
        return GcpBlobStore(storage, gcpProperties)
    }
}