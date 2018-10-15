package com.bol.blueprint.api

import com.bol.blueprint.api.v1.Page
import com.bol.blueprint.api.v1.VersionResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithUserDetails
class VersionResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/namespaces/ns1/schemas/schema1/versions"

    @Test
    fun `Can get versions`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
                .expectBody(typeReference<Page<VersionResource.Responses.Single>>())
            .returnResult()

        assertThat(result.responseBody!!.data).containsExactly(
                VersionResource.Responses.Single(version = "1.0.1"),
                VersionResource.Responses.Single(version = "1.0.0")
        )

        assertThat(result.responseBody!!.total).isEqualTo(2)
    }

    @Test
    fun `Can get single version`() {
        val result = client.get().uri("$baseUrl/1.0.0").exchange()
            .expectStatus().isOk
            .expectBody(VersionResource.Responses.Detail::class.java)
            .returnResult()

        assertThat(result.responseBody).isEqualTo(VersionResource.Responses.Detail(version = "1.0.0"))
    }

    @Test
    fun `Can delete single version`() {
        client.delete().uri("$baseUrl/1.0.0").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/1.0.0").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Cannot get unknown single version`() {
        client.get().uri("$baseUrl/unknown").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create version`() {
        val content = VersionResource.Requests.NewVersion(version = "0.1.2")
        client.post().uri(baseUrl).syncBody(content).exchange().expectStatus().isCreated

        val result = client.get().uri("$baseUrl/0.1.2").exchange().expectStatus().isOk.expectBody(VersionResource.Responses.Detail::class.java).returnResult()
        assertThat(result.responseBody).isEqualTo(VersionResource.Responses.Detail(version = "0.1.2"))
    }

    @Test
    fun `Cannot create duplicate namespace`() {
        val content = VersionResource.Requests.NewVersion(version = "0.1.2")
        client.post().uri(baseUrl).syncBody(content).exchange().expectStatus().isCreated
        client.post().uri(baseUrl).syncBody(content).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}
