package com.bol.blueprint.api

import com.bol.blueprint.api.v1.NamespaceResource
import com.bol.blueprint.api.v1.Page
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithUserDetails
class NamespaceResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/namespaces"

    @Test
    fun `Can get namespaces`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
                .expectBody(typeReference<Page<NamespaceResource.Responses.Summary>>())
            .returnResult()

        expect {
            that(result.responseBody!!.data).containsExactly(
                    NamespaceResource.Responses.Summary(name = "ns1", schemas = listOf("schema1", "schema2")),
                    NamespaceResource.Responses.Summary(name = "ns2", schemas = emptyList())
            )

            that(result.responseBody!!.totalElements).isEqualTo(2)
        }
    }

    @Test
    fun `Can get single namespace`() {
        val result = client.get().uri("$baseUrl/ns1").exchange()
            .expectStatus().isOk
            .expectBody(NamespaceResource.Responses.Detail::class.java)
            .returnResult()

        expectThat(result.responseBody).isEqualTo(NamespaceResource.Responses.Detail(name = "ns1"))
    }

    @Test
    fun `Can delete single namespace`() {
        client.delete().uri("$baseUrl/ns1").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/ns1").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Cannot get unknown single namespace`() {
        client.get().uri("$baseUrl/unknown").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create namespace`() {
        val content = NamespaceResource.Requests.NewNamespace(name = "foo")
        client.post().uri(baseUrl)
            .syncBody(content).exchange().expectStatus().isCreated

        val result = client.get().uri("$baseUrl/foo").exchange().expectStatus().isOk.expectBody(NamespaceResource.Responses.Detail::class.java).returnResult()
        expectThat(result.responseBody).isEqualTo(NamespaceResource.Responses.Detail(name = "foo"))
    }

    @Test
    fun `Cannot create duplicate namespace`() {
        val content = NamespaceResource.Requests.NewNamespace(name = "foo")
        client.post().uri(baseUrl).syncBody(content).exchange().expectStatus().isCreated
        client.post().uri(baseUrl).syncBody(content).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}
