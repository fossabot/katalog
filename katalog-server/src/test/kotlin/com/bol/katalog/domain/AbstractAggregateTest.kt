package com.bol.katalog.domain

import com.bol.katalog.TestData
import com.bol.katalog.applyBasicTestSet
import com.bol.katalog.cqrs.Processor
import com.bol.katalog.cqrs.events.EventPublisher
import com.bol.katalog.domain.aggregates.ArtifactAggregate
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.domain.aggregates.SchemaAggregate
import com.bol.katalog.domain.aggregates.VersionAggregate
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.SecurityProcessor
import com.bol.katalog.store.InMemoryBlobStore
import com.bol.katalog.store.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

abstract class AbstractAggregateTest {
    protected lateinit var processor: DomainProcessor
    private val eventStore = InMemoryEventStore()
    protected val blobStore = InMemoryBlobStore()

    protected val security = SecurityAggregate()
    protected val namespaces = NamespaceAggregate(security)
    protected val schemas = SchemaAggregate()
    protected val versions = VersionAggregate(schemas)
    protected val artifacts = ArtifactAggregate(versions, schemas, blobStore)

    @Before
    fun before() {
        val securityPublisher =
            EventPublisher(
                eventStore,
                listOf(security),
                TestData.clock
            )
        val securityProcessor = SecurityProcessor(Processor(listOf(security), securityPublisher))

        val publisher =
            EventPublisher(
                eventStore,
                listOf(namespaces, artifacts, schemas, versions),
                TestData.clock
            )
        processor = DomainProcessor(Processor(listOf(namespaces, artifacts, schemas, versions), publisher))
        runBlocking { applyBasicTestSet(securityProcessor, processor) }
    }

    @After
    fun after() {
        namespaces.reset()
        schemas.reset()
        versions.reset()
        artifacts.reset()
    }
}