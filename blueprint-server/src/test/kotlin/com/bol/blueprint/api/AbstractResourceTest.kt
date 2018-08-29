package com.bol.blueprint.api

import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.domain.CommandHandler
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient

abstract class AbstractResourceTest {
    @Autowired
    protected lateinit var commandHandler: CommandHandler

    @Autowired
    protected lateinit var applicationContext: ApplicationContext

    protected lateinit var client: WebTestClient

    @Before
    fun superBefore() {
        runBlocking { commandHandler.applyBasicTestSet() }
        client = TestHelper.getClient(applicationContext)
    }
}