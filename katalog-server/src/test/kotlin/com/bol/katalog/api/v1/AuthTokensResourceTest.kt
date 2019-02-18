package com.bol.katalog.api.v1

import com.bol.katalog.api.AbstractResourceTest
import com.bol.katalog.cqrs.sendLocal
import com.bol.katalog.features.registry.Namespace
import com.bol.katalog.features.registry.support.create
import com.bol.katalog.security.allPermissions
import com.bol.katalog.security.support.WithKatalogUser
import com.bol.katalog.security.support.group1
import com.bol.katalog.testing.TestData
import com.bol.katalog.utils.runBlockingAsSystem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@WebFluxTest(AuthTokensResource::class)
@WithKatalogUser("user1")
class AuthTokensResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/auth/tokens"

    private val ns1 = Namespace("id-ns1", "ns1", group1.id, TestData.clock.instant())

    @BeforeEach
    fun before() {
        runBlockingAsSystem {
            context.sendLocal(
                ns1.create()
            )
        }
    }

    @Test
    fun `Can issue and revoke tokens`() {
        val content = AuthTokensResource.Requests.NewToken(
            description = "my token",
            namespaceId = ns1.id,
            permissions = allPermissions()
        )
        val createdResult = exchange<AuthTokensResource.Responses.TokenCreated>(
            method = HttpMethod.POST,
            expect = HttpStatus.CREATED,
            body = content
        )
        val createdId = createdResult!!.id

        val result = exchange<AuthTokensResource.Responses.Token>(path = createdId)
        expectThat(result).isEqualTo(
            AuthTokensResource.Responses.Token(
                id = createdId,
                description = "my token",
                createdOn = TestData.clock.instant()
            )
        )

        exchange(method = HttpMethod.DELETE, path = createdId, expect = HttpStatus.NO_CONTENT)
        exchange(method = HttpMethod.DELETE, path = createdId, expect = HttpStatus.NOT_FOUND)
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot create with insufficient permissions`() {
        val content = AuthTokensResource.Requests.NewToken(
            description = "my token",
            namespaceId = ns1.id,
            permissions = allPermissions()
        )
        exchange(
            method = HttpMethod.POST,
            expect = HttpStatus.FORBIDDEN,
            body = content
        )
    }
}