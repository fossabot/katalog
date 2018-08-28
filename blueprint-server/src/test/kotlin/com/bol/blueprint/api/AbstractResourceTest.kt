package com.bol.blueprint.api

import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.experimental.runBlocking

abstract class AbstractResourceTest {
    protected lateinit var commandHandler: CommandHandler
    protected lateinit var query: Query

    private var eventStore = InMemoryEventStore()
    protected var blobStore = InMemoryBlobStore()

    protected fun superBefore() {
        query = Query()
        commandHandler = CommandHandler(eventStore, blobStore, listOf(query))
        runBlocking { commandHandler.applyBasicTestSet() }
    }
}