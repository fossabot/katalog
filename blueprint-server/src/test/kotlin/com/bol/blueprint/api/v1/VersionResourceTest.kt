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
class VersionResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/versions"

    @Test
    fun `Can get all versions`() {
        val result = client.get().uri(baseUrl).exchange()
                .expectStatus().isOk
                .expectBody(ref<Page<VersionResource.Responses.Version>>())
                .returnResult()

        expect {
            that(result.responseBody!!.data.filter { it.schemaId == TestData.ns1_schema1.id }).containsExactly(
                    VersionResource.Responses.Version(id = TestData.ns1_schema1_v100.id, schemaId = TestData.ns1_schema1.id, version = "1.0.0"),
                    VersionResource.Responses.Version(id = TestData.ns1_schema1_v101.id, schemaId = TestData.ns1_schema1.id, version = "1.0.1"),
                    VersionResource.Responses.Version(id = TestData.ns1_schema1_v200snapshot.id, schemaId = TestData.ns1_schema1.id, version = "2.0.0-SNAPSHOT")
            )

            that(result.responseBody!!.data.filter { it.schemaId == TestData.ns2_schema3.id }).containsExactly(
                    VersionResource.Responses.Version(id = TestData.ns2_schema3_v100.id, schemaId = TestData.ns2_schema3.id, version = "1.0.0")
            )
        }
    }

    @Test
    fun `Can get filtered versions`() {
        val result = client.get().uri {
            it
                    .path(baseUrl)
                    .queryParam("schemaIds", TestData.ns1_schema1.id)
                    .build()
        }.exchange()
                .expectStatus().isOk
                .expectBody(ref<Page<VersionResource.Responses.Version>>())
                .returnResult()

        expect {
            that(result.responseBody!!.data).map { it.version }.containsExactly("1.0.0", "1.0.1", "2.0.0-SNAPSHOT")
        }
    }

    @Test
    fun `Can get single version`() {
        val result = client.get().uri("$baseUrl/${TestData.ns1_schema1_v100.id}").exchange()
                .expectStatus().isOk
                .expectBody(ref<VersionResource.Responses.Version>())
                .returnResult()

        expectThat(result.responseBody).isEqualTo(VersionResource.Responses.Version(id = TestData.ns1_schema1_v100.id, schemaId = TestData.ns1_schema1.id, version = "1.0.0"))
    }

    @Test
    fun `Cannot get unknown single version`() {
        client.get().uri("$baseUrl/${UUID.randomUUID()}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can delete single version`() {
        client.delete().uri("$baseUrl/${TestData.ns1_schema1_v100.id}").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/${TestData.ns1_schema1_v100.id}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create version`() {
        val content = VersionResource.Requests.NewVersion(schemaId = TestData.ns1_schema1.id, version = "2.3.4")
        val createdResult = client.post().uri(baseUrl)
                .syncBody(content)
                .exchange()
                .expectStatus().isCreated
                .expectBody(ref<VersionResource.Responses.VersionCreated>())
                .returnResult()
        val createdId = createdResult.responseBody!!.id

        val result = client.get().uri("$baseUrl/$createdId")
                .exchange()
                .expectStatus().isOk
                .expectBody(ref<VersionResource.Responses.Version>())
                .returnResult()
        expectThat(result.responseBody).isEqualTo(VersionResource.Responses.Version(id = createdId, schemaId = TestData.ns1_schema1.id, version = "2.3.4"))
    }

    @Test
    fun `Cannot create duplicate version`() {
        val content = VersionResource.Requests.NewVersion(schemaId = TestData.ns1_schema1.id, version = "1.0.0")

        client.post().uri(baseUrl)
                .syncBody(content)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}