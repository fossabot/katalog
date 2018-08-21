package com.bol.blueprint

import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class TestApplication {
    @Bean
    fun eventStore() = InMemoryEventStore()

    @Bean
    fun blobStore() = InMemoryBlobStore()
}