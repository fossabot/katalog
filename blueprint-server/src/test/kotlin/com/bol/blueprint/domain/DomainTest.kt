package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.bol.blueprint.TestUsers
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import com.bol.blueprint.store.getBlobStorePath
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

class DomainTest {
    private lateinit var commandHandler: CommandHandler
    private val query: Query = Query()
    private val eventStore = InMemoryEventStore()
    private val blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        val initialHandler = CommandHandler(eventStore, blobStore, emptyList(), TestUsers.user())
        runBlocking { initialHandler.applyBasicTestSet() }

        // Replay the events from the event store
        commandHandler = CommandHandler(eventStore, blobStore, listOf(query), TestUsers.user())
        runBlocking {
            commandHandler.replayFromStore()
        }
    }

    @Test
    fun `Can register namespaces`() {
        expectThat(query.getNamespaces()) {
            hasSize(2)
            hasEntry(TestData.ns1, Namespace("ns1", TestData.group1))
            hasEntry(TestData.ns2, Namespace("ns2", TestData.group1))
        }
    }

    @Test
    fun `Can register schemas`() {
        expectThat(query.getSchemas(listOf(TestData.ns1))) {
            hasSize(2)
            hasEntry(TestData.ns1_schema1, Schema("schema1", SchemaType.default()))
            hasEntry(TestData.ns1_schema2, Schema("schema2", SchemaType.default()))
        }

        expectThat(query.getSchemas(listOf(TestData.ns2))) {
            hasSize(1)
            hasEntry(TestData.ns2_schema3, Schema("schema3", SchemaType.default()))
        }
    }

    @Test
    fun `Can find namespaces of schemas`() {
        expectThat(query.getSchemaNamespace(TestData.ns1_schema1)).isEqualTo(TestData.ns1)
        expectThat(query.getSchemaNamespace(TestData.ns1_schema2)).isEqualTo(TestData.ns1)
        expectThat(query.getSchemaNamespace(TestData.ns2_schema3)).isEqualTo(TestData.ns2)
    }

    @Test
    fun `Can register versions`() {
        expectThat(query.getVersions(listOf(TestData.ns1_schema1))) {
            hasSize(3)
            hasEntry(TestData.ns1_schema1_v100, Version("1.0.0"))
            hasEntry(TestData.ns1_schema1_v101, Version("1.0.1"))
            hasEntry(TestData.ns1_schema1_v200snapshot, Version("2.0.0-SNAPSHOT"))
        }

        expectThat(query.getVersions(listOf(TestData.ns2_schema3))) {
            hasSize(1)
            hasEntry(TestData.ns2_schema3_v100, Version("1.0.0"))
        }
    }

    @Test
    fun `Can find schemas of versions`() {
        expectThat(query.getVersionSchema(TestData.ns1_schema1_v100)).isEqualTo(TestData.ns1_schema1)
        expectThat(query.getVersionSchema(TestData.ns1_schema1_v101)).isEqualTo(TestData.ns1_schema1)
        expectThat(query.getVersionSchema(TestData.ns1_schema1_v200snapshot)).isEqualTo(TestData.ns1_schema1)
        expectThat(query.getVersionSchema(TestData.ns2_schema3_v100)).isEqualTo(TestData.ns2_schema3)
    }

    @Test
    fun `Can register artifacts`() {
        expectThat(query.getArtifacts(listOf(TestData.ns1_schema1_v100))) {
            hasSize(1)
            hasEntry(TestData.artifact1, Artifact("artifact1.json", MediaType.JSON, TestData.artifact1.getBlobStorePath()))
        }

        expectThat(query.getArtifacts(listOf(TestData.ns1_schema1_v101))) {
            hasSize(1)
            hasEntry(TestData.artifact2, Artifact("artifact2.json", MediaType.JSON, TestData.artifact2.getBlobStorePath()))
        }

        runBlocking {
            expectThat(blobStore.get(TestData.artifact1.getBlobStorePath())).isNotNull().contentEquals(byteArrayOf(1, 2, 3))
            expectThat(blobStore.get(TestData.artifact2.getBlobStorePath())).isNotNull().contentEquals(byteArrayOf(4, 5, 6))
        }
    }

    @Test
    fun `Can find versions of artifacts`() {
        expectThat(query.getArtifactVersion(TestData.artifact1)).isEqualTo(TestData.ns1_schema1_v100)
        expectThat(query.getArtifactVersion(TestData.artifact2)).isEqualTo(TestData.ns1_schema1_v101)
    }

    @Test
    fun `Can delete artifact`() {
        runBlocking {
            commandHandler.deleteArtifact(TestData.artifact1)
        }

        expectThat(query.getArtifacts(listOf(TestData.ns1_schema1_v100))).isEmpty()

        runBlocking {
            expectThat(blobStore.get(TestData.artifact1.getBlobStorePath())).isNull()
        }

        expectThat(query.getArtifactVersion(TestData.artifact1)).isNull()
    }

    @Test
    fun `Can delete version`() {
        runBlocking {
            commandHandler.deleteVersion(TestData.ns1_schema1_v100)
        }

        expectThat(query.getVersions(listOf(TestData.ns1_schema1))) {
            hasSize(2)
            hasEntry(TestData.ns1_schema1_v101, Version("1.0.1"))
            hasEntry(TestData.ns1_schema1_v200snapshot, Version("2.0.0-SNAPSHOT"))
        }

        expectThat(query.getVersionSchema(TestData.ns1_schema1_v100)).isNull()
    }

    @Test
    fun `Can delete schema`() {
        runBlocking {
            commandHandler.deleteSchema(TestData.ns1_schema1)
        }

        expectThat(query.getSchemas(listOf(TestData.ns1))) {
            hasSize(1)
            hasEntry(TestData.ns1_schema2, Schema("schema2", SchemaType.default()))
        }

        expectThat(query.getSchemaNamespace(TestData.ns1_schema1)).isNull()
    }

    @Test
    fun `Can delete namespace`() {
        runBlocking {
            commandHandler.deleteNamespace(TestData.ns1)
        }

        expectThat(query.getNamespaces()) {
            hasSize(1)
            hasEntry(TestData.ns2, Namespace("ns2", TestData.group1))
        }
    }
}