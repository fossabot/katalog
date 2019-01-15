package com.bol.katalog.api.v1

import com.bol.katalog.AbstractSpringTest
import com.bol.katalog.security.WithKatalogUser
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.contentEquals

@WithKatalogUser("user1")
class RepositoryResourceTest : AbstractSpringTest() {
    private val baseUrl = "/api/v1/repository"

    @Test
    fun `Can get artifact`() {
        val result = client.get().uri("$baseUrl/ns1/schema1/1.0.0/artifact1.json").exchange()
            .expectStatus().isOk
            .expectBody(ref<ByteArray>())
            .returnResult()

        expectThat(result.responseBody).contentEquals(byteArrayOf(1, 2, 3))
    }
}