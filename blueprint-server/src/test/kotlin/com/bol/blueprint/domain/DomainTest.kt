package com.bol.blueprint.domain

import com.bol.blueprint.SynchronousCommandHandler
import com.bol.blueprint.TestData.NS1
import com.bol.blueprint.TestData.SCHEMA1
import com.bol.blueprint.TestData.VERSION1
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URI

class DomainTest {
    private lateinit var commandHandler: CommandHandler
    private lateinit var query: Query

    private var eventStore = InMemoryEventStore()
    private var blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        query = Query()
        commandHandler = SynchronousCommandHandler(eventStore, blobStore, listOf(query))
        runBlocking { commandHandler.applyBasicTestSet() }
    }

    @Test
    fun `Can register namespaces`() {
        assertThat(query.getNamespaces()).containsExactlyInAnyOrder(
                Namespace("ns1"),
                Namespace("ns2")
        )
    }

    @Test
    fun `Can register schemas`() {
        assertThat(query.getSchemas(NS1)).containsExactlyInAnyOrder(
                Schema("schema1", SchemaType.default()),
                Schema("schema2", SchemaType.default())
        )
    }

    @Test
    fun `Can register versions`() {
        assertThat(query.getVersions(SCHEMA1)).containsExactlyInAnyOrder(
                Version("1.0.0"),
                Version("1.0.1")
        )
    }

    @Test
    fun `Can register artifacts`() {
        val path1 = URI.create("ns1/schema1/1.0.0/artifact1.json")
        val path2 = URI.create("ns1/schema1/1.0.0/artifact2.json")

        assertThat(query.getArtifacts(VERSION1)).containsExactlyInAnyOrder(
                Artifact("artifact1.json", MediaType.JSON, path1),
                Artifact("artifact2.json", MediaType.JSON, path2)
        )

        runBlocking {
            assertThat(blobStore.get(path1)).isEqualTo(byteArrayOf(1, 2, 3))
            assertThat(blobStore.get(path2)).isEqualTo(byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `Can replay from store`() {
        // Replay the events from the event store
        val query = Query()
        val handler2 = SynchronousCommandHandler(eventStore, blobStore, listOf(query))
        runBlocking {
            handler2.replayFromStore()
        }

        // Check the resulting query
        Assertions.assertThat(query.getVersions(SCHEMA1)).containsExactlyInAnyOrder(
                Version("1.0.0"),
                Version("1.0.1")
        )
    }
}