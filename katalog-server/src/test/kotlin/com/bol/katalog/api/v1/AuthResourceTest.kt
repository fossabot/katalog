package com.bol.katalog.api.v1

import com.bol.katalog.cqrs.send
import com.bol.katalog.security.support.WithKatalogUser
import com.bol.katalog.security.support.admin
import com.bol.katalog.security.support.create
import com.bol.katalog.security.support.user1
import com.bol.katalog.security.tokens.auth.TokenService
import com.bol.katalog.utils.runBlockingAsSystem
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@WebFluxTest(AuthResource::class)
class AuthResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/auth"

    @Autowired
    private lateinit var tokenService: TokenService

    @Test
    @WithKatalogUser("user1")
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

    @Test
    fun `Can use token to get logged in user`() {
        runBlockingAsSystem {
            security.send(admin.create())
            security.send(user1.create())
        }

        val token = runBlocking {
            // Create a token as 'admin' for 'user1'
            tokenService.issueToken(admin.id, user1.id)
        }

        // When using the token, we should be logged in as 'user1'
        val result =
            exchange<AuthResource.User>(path = "user-details", headers = mapOf("Authorization" to "Bearer $token"))
        expectThat(result!!).isEqualTo(
            AuthResource.User(
                id = "id-user1",
                username = "user1",
                authorities = setOf("ROLE_USER")
            )
        )
    }
}