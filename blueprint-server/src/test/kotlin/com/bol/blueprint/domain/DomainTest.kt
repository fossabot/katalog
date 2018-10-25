package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.bol.blueprint.TestUsers
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import com.vdurmont.semver4j.Semver
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
        expectThat(query.getNamespaces()).containsExactly(
            Namespace(TestData.ns1, "ns1", TestData.group1),
            Namespace(TestData.ns2, "ns2", TestData.group1)
        )
    }

    @Test
    fun `Can register schemas`() {
        expectThat(query.getSchemas(listOf(TestData.ns1))).containsExactly(
            Schema(TestData.ns1_schema1, "schema1", SchemaType.default()),
            Schema(TestData.ns1_schema2, "schema2", SchemaType.default())
        )

        expectThat(query.getSchemas(listOf(TestData.ns2))).containsExactly(
            Schema(TestData.ns2_schema3, "schema3", SchemaType.default())
        )
    }

    @Test
    fun `Can find namespaces of schemas`() {
        expectThat(query.getSchemas(listOf(TestData.ns1)).map { query.getSchemaNamespace(it) }.distinct().single()).isEqualTo(query.getNamespace(TestData.ns1))
        expectThat(query.getSchemas(listOf(TestData.ns2)).map { query.getSchemaNamespace(it) }.distinct().single()).isEqualTo(query.getNamespace(TestData.ns2))
    }

    @Test
    fun `Can register versions`() {
        expectThat(query.getVersions(TestData.ns1_schema1)).containsExactly(
            Version(TestData.ns1_schema1_v100, Semver("1.0.0", Semver.SemverType.IVY)),
            Version(TestData.ns1_schema1_v101, Semver("1.0.1", Semver.SemverType.IVY)),
            Version(TestData.ns1_schema1_v200snapshot, Semver("2.0.0-SNAPSHOT", Semver.SemverType.IVY))
        )

        expectThat(query.getVersions(TestData.ns2_schema3)).containsExactly(
            Version(TestData.ns2_schema3_v100, Semver("1.0.0", Semver.SemverType.IVY))
        )
    }

    @Test
    fun `Can find schemas of versions`() {
        expectThat(query.getVersions(TestData.ns1_schema1).map { query.getVersionSchema(it) }.distinct().single()).isEqualTo(query.getSchema(TestData.ns1_schema1))
        expectThat(query.getVersions(TestData.ns2_schema3).map { query.getVersionSchema(it) }.distinct().single()).isEqualTo(query.getSchema(TestData.ns2_schema3))
    }

    @Test
    fun `Can register artifacts`() {
        expectThat(query.getArtifacts(listOf(TestData.ns1_schema1_v100))).containsExactly(
            Artifact(TestData.artifact1, "artifact1.json", MediaType.JSON)
        )

        expectThat(query.getArtifacts(listOf(TestData.ns1_schema1_v101))).containsExactly(
            Artifact(TestData.artifact2, "artifact2.json", MediaType.JSON)
        )

        runBlocking {
            expectThat(blobStore.get(TestData.artifact1.getBlobStorePath())).isNotNull().contentEquals(byteArrayOf(1, 2, 3))
            expectThat(blobStore.get(TestData.artifact2.getBlobStorePath())).isNotNull().contentEquals(byteArrayOf(4, 5, 6))
        }
    }

    @Test
    fun `Can find versions of artifacts`() {
        expectThat(query.getArtifacts(listOf(TestData.ns1_schema1_v100)).map { query.getArtifactVersion(it) }.distinct().single()).isEqualTo(query.getVersion(TestData.ns1_schema1_v100))
        expectThat(query.getArtifacts(listOf(TestData.ns1_schema1_v101)).map { query.getArtifactVersion(it) }.distinct().single()).isEqualTo(query.getVersion(TestData.ns1_schema1_v101))
    }

    @Test
    fun `Can delete artifact`() {
        val artifact1 = query.getArtifact(TestData.artifact1)!!

        runBlocking {
            commandHandler.deleteArtifact(TestData.artifact1)
        }

        expectThat(query.getArtifacts(listOf(TestData.ns1_schema1_v100))).isEmpty()

        runBlocking {
            expectThat(blobStore.get(TestData.artifact1.getBlobStorePath())).isNull()
        }

        expectThat(query.getArtifactVersion(artifact1)).isNull()
    }

    @Test
    fun `Can delete version`() {
        val version = query.getVersion(TestData.ns1_schema1_v100)!!

        runBlocking {
            commandHandler.deleteVersion(TestData.ns1_schema1_v100)
        }

        expectThat(query.getVersions(TestData.ns1_schema1)).containsExactly(
            Version(TestData.ns1_schema1_v101, Semver("1.0.1", Semver.SemverType.IVY)),
            Version(TestData.ns1_schema1_v200snapshot, Semver("2.0.0-SNAPSHOT", Semver.SemverType.IVY))
        )

        expectThat(query.getVersionSchema(version)).isNull()
    }

    @Test
    fun `Can delete schema`() {
        val schema = query.getSchema(TestData.ns1_schema1)!!

        runBlocking {
            commandHandler.deleteSchema(TestData.ns1_schema1)
        }

        expectThat(query.getSchemas(listOf(TestData.ns1))).containsExactly(
            Schema(TestData.ns1_schema2, "schema2", SchemaType.default())
        )

        expectThat(query.getSchemaNamespace(schema)).isNull()
    }

    @Test
    fun `Can delete namespace`() {
        runBlocking {
            commandHandler.deleteNamespace(TestData.ns1)
        }

        expectThat(query.getNamespaces()).containsExactly(
            Namespace(TestData.ns2, "ns2", TestData.group1)
        )
    }
}