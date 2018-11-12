package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.bol.blueprint.TestUsers
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.cqrs.CommandPublisher
import com.bol.blueprint.domain.readmodels.NamespaceReadModel
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class NamespaceReadModelTest {
    private lateinit var commandHandler: CommandHandler
    private val namespaces = NamespaceReadModel()
    private val eventStore = InMemoryEventStore()
    private val blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        val publisher = CommandPublisher(eventStore, TestUsers.user(), emptyList(), TestData.clock)
        val initialHandler = CommandHandler(publisher, blobStore)
        runBlocking { initialHandler.applyBasicTestSet() }

        // Replay the events from the event store into 'query'
        val replayPublisher = CommandPublisher(eventStore, TestUsers.user(), listOf(namespaces), TestData.clock)
        commandHandler = CommandHandler(replayPublisher, blobStore)
        runBlocking { replayPublisher.replayFromStore() }
    }

    @Test
    fun `Can register namespaces`() {
        expectThat(namespaces.getNamespaces()).containsExactly(
            Namespace(TestData.ns1, "ns1", TestData.group1),
            Namespace(TestData.ns2, "ns2", TestData.group1)
        )
    }

    @Test
    fun `Can delete namespace`() {
        runBlocking {
            commandHandler.deleteNamespace(TestData.ns1)
        }

        expectThat(namespaces.getNamespaces()).containsExactly(
            Namespace(TestData.ns2, "ns2", TestData.group1)
        )
    }
}