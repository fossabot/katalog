package com.bol.blueprint.api

import com.bol.blueprint.api.v1.ArtifactResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient


class ArtifactResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/namespaces/ns1/schemas/schema1/versions/1.0.0/artifacts"
    private lateinit var client: WebTestClient

    @Before
    fun before() {
        super.superBefore()
        client = WebTestClient.bindToController(ArtifactResource(commandHandler, query, blobStore)).build()
    }

    @Test
    fun `Can get artifacts`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBodyList(ArtifactResource.Responses.Single::class.java)
            .returnResult()

        assertThat(result.responseBody).containsExactly(
            ArtifactResource.Responses.Single(filename = "artifact1.json"),
            ArtifactResource.Responses.Single(filename = "artifact2.json")
        )
    }

    @Test
    fun `Can get single artifact`() {
        val result = client.get().uri("$baseUrl/artifact1.json").exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        assertThat(result.responseBody).isEqualTo(byteArrayOf(1, 2, 3))
    }

    @Test
    fun `Can delete single artifact`() {
        client.delete().uri("$baseUrl/artifact1.json").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/artifact1.json").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Cannot get unknown single artifact`() {
        client.get().uri("$baseUrl/unknown").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create artifact`() {
        val builder = MultipartBodyBuilder()
        builder.part("file", ClassPathResource("api/artifact-example.json"))

        client.post().uri(baseUrl)
            .syncBody(builder.build())
            .exchange()
            .expectStatus().isCreated

        val result = client.get().uri("$baseUrl/artifact-example.json").exchange()
            .expectStatus().isOk
            .expectBody(Responses.ArtifactExampleJson::class.java)
            .returnResult()
        assertThat(result.responseBody).isEqualTo(Responses.ArtifactExampleJson("b"))
    }

    @Test
    fun `Cannot create duplicate artifact`() {
        val builder = MultipartBodyBuilder()
        builder.part("file", ClassPathResource("api/artifact-example.json"))

        client.post().uri(baseUrl).syncBody(builder.build()).exchange().expectStatus().isCreated
        client.post().uri(baseUrl).syncBody(builder.build()).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }

    object Responses {
        data class ArtifactExampleJson(val a: String)
    }
}
