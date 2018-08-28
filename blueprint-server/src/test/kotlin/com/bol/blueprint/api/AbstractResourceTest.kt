package com.bol.blueprint.api

import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.security.web.server.context.SecurityContextServerWebExchangeWebFilter
import org.springframework.test.web.reactive.server.WebTestClient

abstract class AbstractResourceTest {
    protected lateinit var commandHandler: CommandHandler
    protected lateinit var query: Query
    protected lateinit var client: WebTestClient

    private var eventStore = InMemoryEventStore()
    protected var blobStore = InMemoryBlobStore()

    protected fun <T> superBefore(controller: () -> T) {
        query = Query()
        commandHandler = CommandHandler(eventStore, blobStore, listOf(query))
        runBlocking { commandHandler.applyBasicTestSet() }

        client = WebTestClient
            .bindToController(controller.invoke())
            .webFilter<WebTestClient.ControllerSpec>(SecurityContextServerWebExchangeWebFilter())
            .apply<WebTestClient.ControllerSpec>(SecurityMockServerConfigurers.springSecurity())
            .apply<WebTestClient.ControllerSpec>(SecurityMockServerConfigurers.mockUser("foo"))
            .configureClient()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}