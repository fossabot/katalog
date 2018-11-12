package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.bol.blueprint.TestUsers
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.cqrs.CommandPublisher
import com.bol.blueprint.domain.readmodels.SchemaReadModel
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class SchemaReadModelTest {
    private lateinit var commandHandler: CommandHandler
    private val schemas = SchemaReadModel()
    private val eventStore = InMemoryEventStore()
    private val blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        val publisher = CommandPublisher(eventStore, TestUsers.user(), emptyList(), TestData.clock)
        val initialHandler = CommandHandler(publisher, blobStore)
        runBlocking { initialHandler.applyBasicTestSet() }

        // Replay the events from the event store into 'query'
        val replayPublisher = CommandPublisher(eventStore, TestUsers.user(), listOf(schemas), TestData.clock)
        commandHandler = CommandHandler(replayPublisher, blobStore)
        runBlocking { replayPublisher.replayFromStore() }
    }

    @Test
    fun `Can register schemas`() {
        expectThat(schemas.getSchemas(listOf(TestData.ns1))).containsExactly(
            Schema(
                TestData.ns1_schema1,
                "schema1",
                SchemaType.default()
            ),
            Schema(
                TestData.ns1_schema2,
                "schema2",
                SchemaType.default()
            )
        )

        expectThat(schemas.getSchemas(listOf(TestData.ns2))).containsExactly(
            Schema(
                TestData.ns2_schema3,
                "schema3",
                SchemaType.default()
            )
        )
    }

    @Test
    fun `Can find namespaces of schemas`() {
        expectThat(schemas.getSchemas(listOf(TestData.ns1)).map { schemas.getSchemaNamespaceId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns1
        )
        expectThat(schemas.getSchemas(listOf(TestData.ns2)).map { schemas.getSchemaNamespaceId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns2
        )
    }

    @Test
    fun `Can delete schema`() {
        val schema = schemas.getSchema(TestData.ns1_schema1)!!

        runBlocking {
            commandHandler.deleteSchema(TestData.ns1_schema1)
        }

        expectThat(schemas.getSchemas(listOf(TestData.ns1))).containsExactly(
            Schema(
                TestData.ns1_schema2,
                "schema2",
                SchemaType.default()
            )
        )

        expectThat(schemas.getSchemaNamespaceId(schema.id)).isNull()
    }
}