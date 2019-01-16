package com.bol.katalog

import com.bol.katalog.api.TestHelper
import com.bol.katalog.config.KatalogAutoConfiguration
import com.bol.katalog.config.KatalogConfigurationProperties
import com.bol.katalog.config.inmemory.InMemoryBlobStore
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.AggregateManager
import com.bol.katalog.features.registry.RegistryAggregate
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
    protected lateinit var eventStore: InMemoryEventStore

    @Autowired
    protected lateinit var blobStore: InMemoryBlobStore

    @Autowired
    protected lateinit var aggregateManager: AggregateManager

    @Autowired
    protected lateinit var security: SecurityAggregate

    @Autowired
    protected lateinit var registry: RegistryAggregate

    @Autowired
    protected lateinit var applicationContext: ApplicationContext

    protected lateinit var client: WebTestClient

    @Before
    fun superBefore() {
        aggregateManager.start()

        // Apply test data
        runBlocking {
            applyBasicUsersAndGroups(security)

            // Apply test set as user1
            CoroutineUserContext.set(security.read { findUserByUsername("user1") }!!)
            applyBasicTestSet(registry)
        }

        // Initialize webclient
        client = TestHelper.getClient(applicationContext)
    }

    @After
    fun after() {
        aggregateManager.stop()
        eventStore.reset()
        blobStore.reset()
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