package com.bol.katalog.plugin.gcp

import com.bol.katalog.messaging.MessageBus
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.EventStore
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.storage.Storage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GcpProperties::class)
class GcpAutoConfiguration {
    @Bean
    @ConditionalOnProperty("katalog.event-store.type", havingValue = "gcp")
    fun gcpEventStore(storage: Storage): EventStore {
        val datastore: Datastore = DatastoreOptions.newBuilder()
            .setCredentials(storage.options.credentials) // hacky way to get credentials for now
            .build()
            .service
        return GcpEventStore(datastore)
    }

    @Bean
    @ConditionalOnProperty("katalog.blob-store.type", havingValue = "gcp")
    fun gcpBlobStore(storage: Storage, gcpProperties: GcpProperties): BlobStore {
        return GcpBlobStore(storage, gcpProperties)
    }

    @Bean
    @ConditionalOnProperty("katalog.message-bus.type", havingValue = "gcp")
    fun gcpMessageBus(storage: Storage): MessageBus {
        return GcpMessageBus(FixedCredentialsProvider.create(storage.options.credentials), storage.options.projectId)
    }
}