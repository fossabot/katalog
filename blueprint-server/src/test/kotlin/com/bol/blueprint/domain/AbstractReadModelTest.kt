package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.bol.blueprint.TestUsers
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.cqrs.CommandValidator
import com.bol.blueprint.cqrs.EventPublisher
import com.bol.blueprint.domain.readmodels.ArtifactReadModel
import com.bol.blueprint.domain.readmodels.NamespaceReadModel
import com.bol.blueprint.domain.readmodels.SchemaReadModel
import com.bol.blueprint.domain.readmodels.VersionReadModel
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import org.junit.Before

abstract class AbstractReadModelTest {
    protected lateinit var commandHandler: Handler
    protected val namespaces = NamespaceReadModel()
    protected val schemas = SchemaReadModel()
    protected val versions = VersionReadModel(schemas)
    protected val artifacts = ArtifactReadModel(versions, schemas)
    private val eventStore = InMemoryEventStore()
    protected val blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        val validator = CommandValidator(emptyList())
        val publisher = EventPublisher(eventStore, TestUsers.user(), emptyList(), TestData.clock)
        val initialHandler = Handler(validator, publisher, blobStore)
        runBlocking { initialHandler.applyBasicTestSet() }

        // Replay the events from the event store into 'query'
        val replayPublisher =
            EventPublisher(
                eventStore,
                TestUsers.user(),
                listOf(namespaces, artifacts, schemas, versions),
                TestData.clock
            )
        commandHandler = Handler(validator, replayPublisher, blobStore)
        runBlocking { replayPublisher.replayFromStore() }
    }
}