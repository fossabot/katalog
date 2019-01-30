package com.bol.katalog.api.v1

import com.bol.katalog.security.support.WithKatalogUser
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@WebFluxTest(AuthResource::class)
@WithKatalogUser("user1")
class AuthResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/auth"

    @Test
    fun `Can get logged in user`() {
        val result = exchange<AuthResource.User>(path = "user-details")

        expectThat(result!!).isEqualTo(
            AuthResource.User(
                id = "id-user1",
                username = "user1",
                authorities = setOf("ROLE_USER")
            )
        )
    }
}