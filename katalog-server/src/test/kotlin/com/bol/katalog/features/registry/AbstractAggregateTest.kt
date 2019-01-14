package com.bol.katalog.features.registry

import com.bol.katalog.TestData
import com.bol.katalog.applyBasicTestSet
import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.cqrs.events.EventPublisher
import com.bol.katalog.features.registry.aggregates.ArtifactAggregate
import com.bol.katalog.features.registry.aggregates.NamespaceAggregate
import com.bol.katalog.features.registry.aggregates.SchemaAggregate
import com.bol.katalog.features.registry.aggregates.VersionAggregate
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.config.inmemory.InMemoryBlobStore
import com.bol.katalog.config.inmemory.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

abstract class AbstractAggregateTest {
    protected lateinit var processor: CommandProcessor
    private val eventStore = InMemoryEventStore()
    protected val blobStore = InMemoryBlobStore()

    protected val security = SecurityAggregate()
    protected val namespaces = NamespaceAggregate(security)
    protected val schemas = SchemaAggregate()
    protected val versions = VersionAggregate(schemas)
    protected val artifacts = ArtifactAggregate(versions, schemas, blobStore)

    @Before
    fun before() {
        val publisher =
            EventPublisher(
                eventStore,
                listOf(security, namespaces, artifacts, schemas, versions),
                TestData.clock
            )
        processor = CommandProcessor(listOf(security, namespaces, artifacts, schemas, versions), publisher)
        runBlocking { applyBasicTestSet(processor) }
    }

    @After
    fun after() {
        namespaces.reset()
        schemas.reset()
        versions.reset()
        artifacts.reset()
    }
}