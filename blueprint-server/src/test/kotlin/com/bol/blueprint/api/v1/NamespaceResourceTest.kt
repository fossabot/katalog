package com.bol.blueprint.api.v1

import com.bol.blueprint.TestData
import com.bol.blueprint.api.AbstractResourceTest
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
import strikt.assertions.map
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithUserDetails
class NamespaceResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/namespaces"

    @Test
    fun `Can get namespaces`() {
        val result = client.get().uri(baseUrl).exchange()
                .expectStatus().isOk
                .expectBody(ref<Page<NamespaceResource.Responses.Namespace>>())
                .returnResult()

        expect {
            that(result.responseBody!!.data).map { it.namespace }.containsExactly("ns1", "ns2")
        }
    }

    @Test
    fun `Can get single namespace`() {
        val result = client.get().uri("$baseUrl/${TestData.ns1}").exchange()
                .expectStatus().isOk
                .expectBody(ref<NamespaceResource.Responses.Namespace>())
                .returnResult()

        expectThat(result.responseBody).isEqualTo(NamespaceResource.Responses.Namespace(id = TestData.ns1, namespace = "ns1"))
    }

    @Test
    fun `Cannot get unknown single namespace`() {
        client.get().uri("$baseUrl/${UUID.randomUUID()}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can find single namespace by name`() {
        val result = client.get().uri("$baseUrl/find/ns1").exchange()
            .expectStatus().isOk
            .expectBody(ref<NamespaceResource.Responses.Namespace>())
            .returnResult()

        expectThat(result.responseBody).isEqualTo(NamespaceResource.Responses.Namespace(id = TestData.ns1, namespace = "ns1"))
    }

    @Test
    fun `Cannot find unknown single namespace by name`() {
        client.get().uri("$baseUrl/find/unknown").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can delete single namespace`() {
        client.delete().uri("$baseUrl/${TestData.ns1}").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/${TestData.ns1}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create namespace`() {
        val content = NamespaceResource.Requests.NewNamespace(namespace = "foo")
        val createdResult = client.post().uri(baseUrl)
                .syncBody(content)
                .exchange()
                .expectStatus().isCreated
                .expectBody(ref<NamespaceResource.Responses.NamespaceCreated>())
                .returnResult()
        val createdId = createdResult.responseBody!!.id

        val result = client.get().uri("$baseUrl/$createdId")
                .exchange()
                .expectStatus().isOk
                .expectBody(ref<NamespaceResource.Responses.Namespace>())
                .returnResult()
        expectThat(result.responseBody).isEqualTo(NamespaceResource.Responses.Namespace(id = createdId, namespace = "foo"))
    }

    @Test
    fun `Cannot create duplicate namespace`() {
        val content = NamespaceResource.Requests.NewNamespace(namespace = "ns1")

        client.post().uri(baseUrl)
                .syncBody(content)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}