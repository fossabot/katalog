package com.bol.katalog

import com.bol.katalog.api.TestHelper
import com.bol.katalog.config.KatalogAutoConfiguration
import com.bol.katalog.config.KatalogConfigurationProperties
import com.bol.katalog.config.inmemory.InMemoryBlobStore
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.SecurityAggregate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractSpringTest {
    @Autowired
    protected lateinit var processor: CommandProcessor

    @Autowired
    protected lateinit var security: SecurityAggregate

    @Autowired
    protected lateinit var applicationContext: ApplicationContext

    protected lateinit var client: WebTestClient

    @Before
    fun superBefore() {
        // Apply test data
        runBlocking {
            applyBasicUsersAndGroups(processor)

            // Apply test set as user1
            CoroutineUserContext.set(security.findUserByUsername("user1")!!)
            applyBasicTestSet(processor)
        }

        // Initialize webclient
        client = TestHelper.getClient(applicationContext)
    }

    @After
    fun after() {
        applicationContext.getBeansOfType(Resettable::class.java).values.forEach { it.reset() }
    }

    final inline fun <reified T> ref() = object : ParameterizedTypeReference<T>() {}

    @SpringBootApplication
    @EnableConfigurationProperties(KatalogConfigurationProperties::class)
    @ImportAutoConfiguration(KatalogAutoConfiguration::class)
    class TestSpringApplication {
        @Bean
        fun eventStore() = InMemoryEventStore()

        @Bean
        fun blobStore() = InMemoryBlobStore()

        @Bean
        fun clock() = TestData.clock
    }
}