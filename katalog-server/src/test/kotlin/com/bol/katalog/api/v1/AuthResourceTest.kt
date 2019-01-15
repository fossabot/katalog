package com.bol.katalog.api.v1

import com.bol.katalog.AbstractSpringTest
import com.bol.katalog.security.WithKatalogUser
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@WithKatalogUser("user1")
class AuthResourceTest : AbstractSpringTest() {
    private val baseUrl = "/api/v1/auth"

    @Test
    fun `Can get logged in user`() {
        val result = client.get().uri("$baseUrl/user-details").exchange()
            .expectStatus().isOk
            .expectBody(ref<AuthResource.User>())
            .returnResult()

        expectThat(result.responseBody!!).isEqualTo(
            AuthResource.User(
                id = "id-user1",
                username = "user1",
                authorities = setOf("ROLE_USER")
            )
        )
    }
}