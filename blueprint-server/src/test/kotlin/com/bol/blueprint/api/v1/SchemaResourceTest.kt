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
class SchemaResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/schemas"

    @Test
    fun `Can get all schemas`() {
        val result = client.get().uri(baseUrl).exchange()
                .expectStatus().isOk
                .expectBody(ref<Page<SchemaResource.Responses.Schema>>())
                .returnResult()

        expect {
            that(result.responseBody!!.data).containsExactly(
                    SchemaResource.Responses.Schema(id = TestData.ns1_schema1.id, namespaceId = TestData.ns1.id, schema = "schema1"),
                    SchemaResource.Responses.Schema(id = TestData.ns1_schema2.id, namespaceId = TestData.ns1.id, schema = "schema2"),
                    SchemaResource.Responses.Schema(id = TestData.ns2_schema3.id, namespaceId = TestData.ns2.id, schema = "schema3")
            )
        }
    }

    @Test
    fun `Can get filtered schemas`() {
        val result = client.get().uri {
            it
                    .path(baseUrl)
                    .queryParam("namespaceIds", TestData.ns1.id)
                    .build()
        }.exchange()
                .expectStatus().isOk
                .expectBody(ref<Page<SchemaResource.Responses.Schema>>())
                .returnResult()

        expect {
            that(result.responseBody!!.data).map { it.schema }.containsExactly("schema1", "schema2")
        }
    }

    @Test
    fun `Can get single schema`() {
        val result = client.get().uri("$baseUrl/${TestData.ns1_schema1.id}").exchange()
                .expectStatus().isOk
                .expectBody(ref<SchemaResource.Responses.Schema>())
                .returnResult()

        expectThat(result.responseBody).isEqualTo(SchemaResource.Responses.Schema(id = TestData.ns1_schema1.id, namespaceId = TestData.ns1.id, schema = "schema1"))
    }

    @Test
    fun `Cannot get unknown single namespace`() {
        client.get().uri("$baseUrl/${UUID.randomUUID()}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can delete single schema`() {
        client.delete().uri("$baseUrl/${TestData.ns1_schema1.id}").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/${TestData.ns1_schema1.id}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create schema`() {
        val content = SchemaResource.Requests.NewSchema(namespaceId = TestData.ns1.id, schema = "foo")
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
        expectThat(result.responseBody).isEqualTo(SchemaResource.Responses.Schema(id = createdId, namespaceId = TestData.ns1.id, schema = "foo"))
    }

    @Test
    fun `Cannot create duplicate namespace`() {
        val content = SchemaResource.Requests.NewSchema(namespaceId = TestData.ns1.id, schema = "schema1")

        client.post().uri(baseUrl)
                .syncBody(content)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}