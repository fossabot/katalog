package com.bol.katalog.config

import com.bol.katalog.security.config.SecurityConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "katalog")
class KatalogConfigurationProperties {
    var security = SecurityConfigurationProperties()

    @Suppress("unused")
    var eventStore = EventStoreProperties()

    @Suppress("unused")
    var blobStore = BlobStoreProperties()

    @Suppress("unused")
    var messageBus = MessageBusProperties()

    @Suppress("unused")
    var clustering = ClusteringProperties()

    inner class EventStoreProperties {
        lateinit var type: String
    }

    inner class BlobStoreProperties {
        lateinit var type: String
    }

    inner class MessageBusProperties {
        lateinit var type: String
    }

    inner class ClusteringProperties {
        lateinit var type: String
    }
}