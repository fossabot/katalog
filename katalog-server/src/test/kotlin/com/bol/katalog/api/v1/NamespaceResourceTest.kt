package com.bol.katalog.api.v1

import com.bol.katalog.TestData
import com.bol.katalog.api.AbstractResourceTest
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
            .expectBody(ref<PageResponse<NamespaceResource.Responses.Namespace>>())
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

        expectThat(result.responseBody).isEqualTo(
            NamespaceResource.Responses.Namespace(
                id = TestData.ns1,
                group = "group1",
                namespace = "ns1",
                createdOn = TestData.clock.instant()
            )
        )
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

        expectThat(result.responseBody).isEqualTo(
            NamespaceResource.Responses.Namespace(
                id = TestData.ns1,
                group = "group1",
                namespace = "ns1",
                createdOn = TestData.clock.instant()
            )
        )
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
        val content = NamespaceResource.Requests.NewNamespace(namespace = "foo", group = "group1")
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
        expectThat(result.responseBody).isEqualTo(
            NamespaceResource.Responses.Namespace(
                id = createdId,
                group = "group1",
                namespace = "foo",
                createdOn = TestData.clock.instant()
            )
        )
    }

    @Test
    @WithUserDetails("no-groups-user")
    fun `Cannot create namespace with insufficient rights`() {
        val content = NamespaceResource.Requests.NewNamespace(namespace = "foo", group = "group1")
        client.post().uri(baseUrl)
            .syncBody(content)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `Cannot create duplicate namespace`() {
        val content = NamespaceResource.Requests.NewNamespace(namespace = "ns1", group = "group1")

        client.post().uri(baseUrl)
            .syncBody(content)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}