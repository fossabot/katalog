package com.bol.blueprint.domain

import com.bol.blueprint.TestData.NS1
import com.bol.blueprint.TestData.SCHEMA1
import com.bol.blueprint.TestData.VERSION1
import com.bol.blueprint.TestUsers
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.contentEquals
import strikt.assertions.isNotNull
import java.net.URI

class DomainTest {
    private lateinit var commandHandler: CommandHandler
    private lateinit var query: Query

    private var eventStore = InMemoryEventStore()
    private var blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        query = Query()
        commandHandler = CommandHandler(eventStore, blobStore, listOf(query), TestUsers.user())
        runBlocking { commandHandler.applyBasicTestSet() }
    }

    @Test
    fun `Can register namespaces`() {
        expectThat(query.getNamespaces().toSet()).containsExactlyInAnyOrder(
                Namespace("ns1", GroupKey("group1")),
                Namespace("ns2", GroupKey("group1"))
        )
    }

    @Test
    fun `Can register schemas`() {
        expectThat(query.getSchemas(NS1).toSet()).containsExactlyInAnyOrder(
                Schema("schema1", SchemaType.default()),
                Schema("schema2", SchemaType.default())
        )
    }

    @Test
    fun `Can register versions`() {
        expectThat(query.getVersions(SCHEMA1).toSet()).containsExactlyInAnyOrder(
                Version("1.0.0"),
                Version("1.0.1")
        )
    }

    @Test
    fun `Can register artifacts`() {
        val path1 = URI.create("ns1/schema1/1.0.0/artifact1.json")
        val path2 = URI.create("ns1/schema1/1.0.0/artifact2.json")

        expectThat(query.getArtifacts(VERSION1).toSet()).containsExactlyInAnyOrder(
                Artifact("artifact1.json", MediaType.JSON, path1),
                Artifact("artifact2.json", MediaType.JSON, path2)
        )

        runBlocking {
            expectThat(blobStore.get(path1)).isNotNull().contentEquals(byteArrayOf(1, 2, 3))
            expectThat(blobStore.get(path2)).isNotNull().contentEquals(byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `Can replay from store`() {
        // Replay the events from the event store
        val query = Query()
        val handler2 = CommandHandler(eventStore, blobStore, listOf(query), TestUsers.user())
        runBlocking {
            handler2.replayFromStore()
        }

        // Check the resulting query
        expectThat(query.getVersions(SCHEMA1).toSet()).containsExactlyInAnyOrder(
                Version("1.0.0"),
                Version("1.0.1")
        )
    }

    @Test
    fun `Can query version ranges`() {

    }
}