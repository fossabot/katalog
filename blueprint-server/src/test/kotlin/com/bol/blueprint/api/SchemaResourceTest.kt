package com.bol.blueprint.api

import com.bol.blueprint.api.v1.SchemaResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

class SchemaResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/namespaces/ns1/schemas"
    private lateinit var client: WebTestClient

    @Before
    fun before() {
        super.superBefore()
        client = WebTestClient.bindToController(SchemaResource(commandHandler, query)).build()
    }

    @Test
    fun `Can get schemas`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBodyList(SchemaResource.Responses.Single::class.java)
            .returnResult()

        assertThat(result.responseBody).containsExactly(
            SchemaResource.Responses.Single(name = "schema1"),
            SchemaResource.Responses.Single(name = "schema2")
        )
    }

    @Test
    fun `Can get single schema`() {
        val result = client.get().uri("$baseUrl/schema1").exchange()
            .expectStatus().isOk
            .expectBody(SchemaResource.Responses.Detail::class.java)
            .returnResult()

        assertThat(result.responseBody).isEqualTo(SchemaResource.Responses.Detail(name = "schema1"))
    }

    @Test
    fun `Can delete single schema`() {
        client.delete().uri("$baseUrl/schema1").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/schema1").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Cannot get unknown single schema`() {
        client.get().uri("$baseUrl/unknown").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create schema`() {
        val content = SchemaResource.Requests.NewSchema(name = "foo")
        client.post().uri(baseUrl).syncBody(content).exchange().expectStatus().isCreated

        val result = client.get().uri("$baseUrl/foo").exchange().expectStatus().isOk.expectBody(SchemaResource.Responses.Detail::class.java).returnResult()
        assertThat(result.responseBody).isEqualTo(SchemaResource.Responses.Detail(name = "foo"))
    }

    @Test
    fun `Cannot create duplicate schema`() {
        val content = SchemaResource.Requests.NewSchema(name = "foo")
        client.post().uri(baseUrl).syncBody(content).exchange().expectStatus().isCreated
        client.post().uri(baseUrl).syncBody(content).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}
