package com.bol.katalog

import com.bol.katalog.config.inmemory.InMemoryBlobStore
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.cqrs.PublishingCommandProcessor
import com.bol.katalog.cqrs.events.EventPublisher
import com.bol.katalog.features.registry.aggregates.ArtifactAggregate
import com.bol.katalog.features.registry.aggregates.NamespaceAggregate
import com.bol.katalog.features.registry.aggregates.SchemaAggregate
import com.bol.katalog.features.registry.aggregates.VersionAggregate
import com.bol.katalog.security.SecurityAggregate
import kotlinx.coroutines.runBlocking

/**
 * This class can be used to do non-Spring-related tests which simply require an in-memory application
 * without any external Spring configuration. Ideal for testing business logic.
 */
object TestApplication {
    lateinit var processor: TestProcessor

    lateinit var eventStore: InMemoryEventStore
    lateinit var blobStore: InMemoryBlobStore

    lateinit var security: SecurityAggregate
    lateinit var namespaces: NamespaceAggregate
    lateinit var schemas: SchemaAggregate
    lateinit var versions: VersionAggregate
    lateinit var artifacts: ArtifactAggregate

    fun reset(applyTestData: Boolean = true) {
        eventStore = InMemoryEventStore()
        blobStore = InMemoryBlobStore()

        security = SecurityAggregate()
        namespaces = NamespaceAggregate(security)
        schemas = SchemaAggregate()
        versions = VersionAggregate(schemas)
        artifacts = ArtifactAggregate(versions, schemas, blobStore)

        val publisher =
            EventPublisher(
                eventStore,
                listOf(security, namespaces, artifacts, schemas, versions),
                TestData.clock
            )
        val actualProcessor =
            PublishingCommandProcessor(listOf(security, namespaces, artifacts, schemas, versions), publisher)
        processor = TestProcessor(actualProcessor)

        if (applyTestData) {
            runBlocking {
                applyBasicUsersAndGroups(processor)
                applyBasicTestSet(processor)
            }
        }
    }

    class TestProcessor(private val delegate: CommandProcessor) : CommandProcessor {
        val received: MutableList<Command> = mutableListOf()

        override suspend fun <TCommand : Command> apply(command: TCommand) {
            received += command
            delegate.apply(command)
        }

        fun clearReceivedEvents() {
            received.clear()
        }
    }
}