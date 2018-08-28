package com.bol.blueprint.domain

import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.Page
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.security.Principal

class CommandHandlerTest {
    @Test
    fun `Can register event with username`() {
        class TestEventStore : EventStore {
            override suspend fun get(query: EventQuery): Page<Event<Any>> = throw UnsupportedOperationException()
            override suspend fun <T : Any> store(event: Event<T>) {
                assertThat(event.metadata.username).isEqualTo("foo")
            }
        }

        val commandHandler = CommandHandler(TestEventStore(), InMemoryBlobStore(), listOf())
        runBlocking {
            commandHandler.withPrincipal(Objects.TestPrincipal("foo")) { createNamespace(NamespaceKey("test")) }
        }
    }

    object Objects {
        data class TestPrincipal(val username: String) : Principal {
            override fun getName() = username
        }
    }
}