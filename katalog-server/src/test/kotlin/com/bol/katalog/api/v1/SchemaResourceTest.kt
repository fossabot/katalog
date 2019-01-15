package com.bol.katalog.api.v1

import com.bol.katalog.AbstractSpringTest
import com.bol.katalog.TestData
import com.bol.katalog.api.PageResponse
import com.bol.katalog.security.WithKatalogUser
import org.junit.Test
import org.springframework.http.HttpStatus
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.map
import java.util.*

@WithKatalogUser("user1")
class SchemaResourceTest : AbstractSpringTest() {
    private val baseUrl = "/api/v1/schemas"

    @Test
    fun `Can get all schemas`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<PageResponse<SchemaResource.Responses.Schema>>())
            .returnResult()

        expect {
            that(result.responseBody!!.data).containsExactly(
                SchemaResource.Responses.Schema(
                    id = "id-ns1-schema1",
                    createdOn = TestData.clock.instant(),
                    namespace = SchemaResource.Responses.Schema.Namespace("id-ns1", "ns1"),
                    schema = "schema1"
                ),
                SchemaResource.Responses.Schema(
                    id = "id-ns1-schema2",
                    createdOn = TestData.clock.instant(),
                    namespace = SchemaResource.Responses.Schema.Namespace("id-ns1", "ns1"),
                    schema = "schema2"
                ),
                SchemaResource.Responses.Schema(
                    id = "id-ns2-schema3",
                    createdOn = TestData.clock.instant(),
                    namespace = SchemaResource.Responses.Schema.Namespace("id-ns2", "ns2"),
                    schema = "schema3"
                )
            )
        }
    }

    @Test
    fun `Can get filtered schemas`() {
        val result = client.get().uri {
            it
                .path(baseUrl)
                .queryParam("namespaceIds", "id-ns1")
                .build()
        }.exchange()
            .expectStatus().isOk
            .expectBody(ref<PageResponse<SchemaResource.Responses.Schema>>())
            .returnResult()

        expect {
            that(result.responseBody!!.data).map { it.schema }.containsExactly("schema1", "schema2")
        }
    }

    @Test
    fun `Can get single schema`() {
        val result = client.get().uri("$baseUrl/id-ns1-schema1").exchange()
            .expectStatus().isOk
            .expectBody(ref<SchemaResource.Responses.Schema>())
            .returnResult()

        expectThat(result.responseBody).isEqualTo(
            SchemaResource.Responses.Schema(
                id = "id-ns1-schema1",
                createdOn = TestData.clock.instant(),
                namespace = SchemaResource.Responses.Schema.Namespace("id-ns1", "ns1"),
                schema = "schema1"
            )
        )
    }

    @Test
    fun `Cannot get unknown single schema`() {
        client.get().uri("$baseUrl/${UUID.randomUUID()}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can find single schema by name`() {
        val result = client.get().uri("$baseUrl/find/ns1/schema1").exchange()
            .expectStatus().isOk
            .expectBody(ref<SchemaResource.Responses.Schema>())
            .returnResult()

        expectThat(result.responseBody).isEqualTo(
            SchemaResource.Responses.Schema(
                id = "id-ns1-schema1",
                createdOn = TestData.clock.instant(),
                namespace = SchemaResource.Responses.Schema.Namespace("id-ns1", "ns1"),
                schema = "schema1"
            )
        )
    }

    @Test
    fun `Cannot find unknown single schema by name`() {
        client.get().uri("$baseUrl/find/unknown").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can delete single schema`() {
        client.delete().uri("$baseUrl/id-ns1-schema1").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/id-ns1-schema1").exchange().expectStatus().isNotFound
    }

    @Test
    @WithKatalogUser("no-groups-user")
    fun `Cannot delete single schema with insufficient permissions`() {
        client.delete().uri("$baseUrl/id-ns1-schema1").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create schema`() {
        val content = SchemaResource.Requests.NewSchema(namespaceId = "id-ns1", schema = "foo")
        val createdResult = client.post().uri(baseUrl)
            .syncBody(content)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ref<SchemaResource.Responses.SchemaCreated>())
            .returnResult()
        val createdId = createdResult.responseBody!!.id

        val result = client.get().uri("$baseUrl/$createdId")
            .exchange()
            .expectStatus().isOk
            .expectBody(ref<SchemaResource.Responses.Schema>())
            .returnResult()
        expectThat(result.responseBody).isEqualTo(
            SchemaResource.Responses.Schema(
                id = createdId,
                createdOn = TestData.clock.instant(),
                namespace = SchemaResource.Responses.Schema.Namespace("id-ns1", "ns1"),
                schema = "foo"
            )
        )
    }

    @Test
    @WithKatalogUser("no-groups-user")
    fun `Cannot create schema with insufficient permissions`() {
        val content = SchemaResource.Requests.NewSchema(namespaceId = "id-ns1", schema = "foo")
        client.post().uri(baseUrl)
            .syncBody(content)
            .exchange()
            .expectStatus().isNotFound // namespace cannot be found with these permissions
    }

    @Test
    fun `Cannot create duplicate schema`() {
        val content = SchemaResource.Requests.NewSchema(namespaceId = "id-ns1", schema = "schema1")

        client.post().uri(baseUrl)
            .syncBody(content)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}