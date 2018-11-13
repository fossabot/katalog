package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.bol.blueprint.TestUsers
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.cqrs.CommandProcessor
import com.bol.blueprint.cqrs.commands.CommandValidator
import com.bol.blueprint.cqrs.events.EventPublisher
import com.bol.blueprint.domain.aggregates.ArtifactAggregate
import com.bol.blueprint.domain.aggregates.NamespaceAggregate
import com.bol.blueprint.domain.aggregates.SchemaAggregate
import com.bol.blueprint.domain.aggregates.VersionAggregate
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

abstract class AbstractReadModelTest {
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