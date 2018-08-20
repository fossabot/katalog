package com.bol.blueprint.plugin.gcp

import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.storage.Storage
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GcpProperties::class)
class GcpAutoConfiguration {
    @Bean
    fun gcpEventStore(storage: Storage): EventStore {
        val datastore: Datastore = DatastoreOptions.newBuilder()
            .setCredentials(storage.options.credentials) // hacky way to get credentials for now
            .build()
            .service
        return GcpEventStore(datastore)
    }

    @Bean
    fun gcpBlobStore(storage: Storage, gcpProperties: GcpProperties): BlobStore {
        return GcpBlobStore(storage, gcpProperties)
    }
}