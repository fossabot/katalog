package com.bol.blueprint

import com.bol.blueprint.domain.*
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import java.net.URI

class DomainTest {
    companion object {
        val NS1 = NamespaceKey("ns1")
        val NS2 = NamespaceKey("ns2")
        val SCHEMA1 = SchemaKey("ns1", "schema1")
        val SCHEMA2 = SchemaKey("ns1", "schema2")
        val VERSION1 = VersionKey("ns1", "schema1", "1.0.0")
        val ARTIFACT1 = ArtifactKey("ns1", "schema1", "1.0.0", "artifact1.json")
    }

    private lateinit var dispatcher: Dispatcher
    private lateinit var query: Query

    private var blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        dispatcher = Dispatcher(InMemoryEventStore(), blobStore)
        query = Query()
        dispatcher.addListener(query)
    }

    @Test
    fun `Can register namespaces`() {
        launch {
            dispatcher.createNamespace(NS1)
            dispatcher.createNamespace(NS2)
        }

        await().untilAsserted {
            assertThat(query.getNamespaces()).containsExactlyInAnyOrder(
                Namespace("ns1"),
                Namespace("ns2")
            )
        }
    }

    @Test
    fun `Can register schemas`() {
        launch {
            dispatcher.createNamespace(NS1)
            dispatcher.createSchema(SCHEMA1, SchemaType.default())
            dispatcher.createSchema(SCHEMA2, SchemaType.default())
        }

        await().untilAsserted {
            assertThat(query.getSchemas(NS1)).containsExactlyInAnyOrder(
                Schema("schema1", SchemaType.default()),
                Schema("schema2", SchemaType.default())
            )
        }
    }

    @Test
    fun `Can register versions`() {
        launch {
            dispatcher.createNamespace(NS1)
            dispatcher.createSchema(SCHEMA1, SchemaType.default())
            dispatcher.createVersion(VERSION1)
        }

        await().untilAsserted {
            assertThat(query.getVersions(SCHEMA1)).containsExactlyInAnyOrder(
                Version("1.0.0")
            )
        }
    }

    @Test
    fun `Can register artifact`() {
        launch {
            dispatcher.createNamespace(NS1)
            dispatcher.createSchema(SCHEMA1, SchemaType.default())
            dispatcher.createVersion(VERSION1)
            dispatcher.createArtifact(ARTIFACT1, MediaType.JSON, byteArrayOf(1, 2, 3))
        }

        val path = URI.create("ns1/schema1/1.0.0/artifact1.json")

        await().untilAsserted {
            assertThat(query.getArtifacts(VERSION1)).containsExactlyInAnyOrder(
                Artifact("artifact1.json", MediaType.JSON, path)
            )
        }

        runBlocking {
            assertThat(blobStore.get(path)).isEqualTo(byteArrayOf(1, 2, 3))
        }
    }


    @Test
    fun `Can replay from store`() {
        val dispatcher = Dispatcher(InMemoryEventStore(), InMemoryBlobStore())

        // Create some events
        runBlocking {
            dispatcher.createNamespace(DomainTest.NS1)
            dispatcher.createSchema(DomainTest.SCHEMA1, SchemaType.default())
            dispatcher.createVersion(DomainTest.VERSION1)
        }

        // Replay them from the store
        val query = Query()
        dispatcher.addListener(query)
        runBlocking {
            dispatcher.replayFromStore()
        }

        // Check the resulting query
        Awaitility.await().untilAsserted {
            Assertions.assertThat(query.getVersions(DomainTest.SCHEMA1)).containsExactlyInAnyOrder(
                Version("1.0.0")
            )
        }
    }
}