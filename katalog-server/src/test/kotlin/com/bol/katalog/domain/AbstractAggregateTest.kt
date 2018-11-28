package com.bol.katalog.domain

import com.bol.katalog.TestData
import com.bol.katalog.TestUsers
import com.bol.katalog.applyBasicTestSet
import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.cqrs.commands.CommandValidator
import com.bol.katalog.cqrs.events.EventPublisher
import com.bol.katalog.domain.aggregates.ArtifactAggregate
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.domain.aggregates.SchemaAggregate
import com.bol.katalog.domain.aggregates.VersionAggregate
import com.bol.katalog.store.InMemoryBlobStore
import com.bol.katalog.store.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

abstract class AbstractAggregateTest {
    protected lateinit var processor: Processor
    protected val namespaces = NamespaceAggregate()
    protected val schemas = SchemaAggregate()
    protected val versions = VersionAggregate(schemas)
    protected val artifacts = ArtifactAggregate(versions, schemas)
    private val eventStore = InMemoryEventStore()
    protected val blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        val validator = CommandValidator(emptyList())
        val publisher =
            EventPublisher(
                eventStore, TestUsers.user(),
                listOf(namespaces, artifacts, schemas, versions), TestData.clock
            )
        processor = Processor(CommandProcessor(validator, publisher), blobStore)
        runBlocking { processor.applyBasicTestSet() }
    }

    @After
    fun after() {
        namespaces.reset()
        schemas.reset()
        versions.reset()
        artifacts.reset()
    }
}