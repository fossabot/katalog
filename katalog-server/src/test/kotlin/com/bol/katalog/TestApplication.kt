package com.bol.katalog

import com.bol.katalog.config.inmemory.InMemoryBlobStore
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.AggregateManager
import com.bol.katalog.cqrs.clustering.inmemory.InMemoryClusteringContext
import com.bol.katalog.features.registry.RegistryAggregate
import com.bol.katalog.security.SecurityAggregate
import kotlinx.coroutines.runBlocking

/**
 * This class can be used to do non-Spring-related tests which simply require an in-memory application
 * without any external Spring configuration. Ideal for testing business logic.
 */
object TestApplication {
    private val eventStore = InMemoryEventStore()
    val blobStore = InMemoryBlobStore()
    val security = SecurityAggregate()
    val registry = RegistryAggregate(security, blobStore)
    val clustering = InMemoryClusteringContext(eventStore, TestData.clock)
    private val aggregateManager = AggregateManager(listOf(security, registry), eventStore, clustering)

    fun reset(applyTestData: Boolean = true) {
        eventStore.reset()
        blobStore.reset()
        aggregateManager.stop()
        aggregateManager.start()

        if (applyTestData) {
            runBlocking {
                applyBasicUsersAndGroups(security)
                applyBasicTestSet(registry)
            }
        }
    }
}