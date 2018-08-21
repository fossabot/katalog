package com.bol.blueprint.domain

import com.bol.blueprint.TestData.ARTIFACT1
import com.bol.blueprint.TestData.NS1
import com.bol.blueprint.TestData.NS2
import com.bol.blueprint.TestData.SCHEMA1
import com.bol.blueprint.TestData.SCHEMA2
import com.bol.blueprint.TestData.VERSION1
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import java.net.URI

class DomainTest {
    private lateinit var dispatcher: Dispatcher
    private lateinit var query: Query

    private var eventStore = InMemoryEventStore()
    private var blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        query = Query()
        dispatcher = Dispatcher(eventStore, blobStore, listOf(query))
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
        // Create some events
        runBlocking {
            dispatcher.createNamespace(NS1)
            dispatcher.createSchema(SCHEMA1, SchemaType.default())
            dispatcher.createVersion(VERSION1)
        }

        // Replay them from the store
        val query = Query()
        val dispatcher2 = Dispatcher(eventStore, blobStore, listOf(query))
        runBlocking {
            dispatcher2.replayFromStore()
        }

        // Check the resulting query
        await().untilAsserted {
            Assertions.assertThat(query.getVersions(SCHEMA1)).containsExactlyInAnyOrder(
                Version("1.0.0")
            )
        }
    }
}