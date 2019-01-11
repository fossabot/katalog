package com.bol.katalog.api

import com.bol.katalog.applyBasicTestSet
import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.cqrs.Resettable
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.reactive.server.WebTestClient

abstract class AbstractResourceTest {
    @Autowired
    protected lateinit var processor: CommandProcessor

    @Autowired
    protected lateinit var applicationContext: ApplicationContext

    protected lateinit var client: WebTestClient

    @Before
    fun superBefore() {
        runBlocking { applyBasicTestSet(processor) }
        client = TestHelper.getClient(applicationContext)
    }

    @After
    fun after() {
        applicationContext.getBeansOfType(Resettable::class.java).values.forEach { it.reset() }
    }

    inline fun <reified T> ref() = object : ParameterizedTypeReference<T>() {}
}