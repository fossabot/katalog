package com.bol.blueprint.api.v1

import com.bol.blueprint.TestData
import com.bol.blueprint.api.AbstractResourceTest
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.web.util.UriBuilder
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
    fun `Can get all versions, not filtered on major version`() {
        val result = getFilteredVersions { it.queryParam("latestPerMajorVersion", false) }

        expect {
            that(result.responseBody!!.data.filter { it.schemaId == TestData.ns1_schema1 }).containsExactly(
                    VersionResource.Responses.Version(id = TestData.ns1_schema1_v200snapshot, schemaId = TestData.ns1_schema1, version = "2.0.0-SNAPSHOT"),
                    VersionResource.Responses.Version(id = TestData.ns1_schema1_v101, schemaId = TestData.ns1_schema1, version = "1.0.1"),
                    VersionResource.Responses.Version(id = TestData.ns1_schema1_v100, schemaId = TestData.ns1_schema1, version = "1.0.0")
            )

            that(result.responseBody!!.data.filter { it.schemaId == TestData.ns2_schema3 }).containsExactly(
                    VersionResource.Responses.Version(id = TestData.ns2_schema3_v100, schemaId = TestData.ns2_schema3, version = "1.0.0")
            )
        }
    }

    @Test
    fun `Can get versions, filtered on schema`() {
        val result = getFilteredVersions { it.queryParam("schemaIds", TestData.ns1_schema1) }

        expect {
            that(result.responseBody!!.data).map { it.version }.containsExactly("2.0.0-SNAPSHOT", "1.0.1")
        }
    }

    @Test
    fun `Can get versions, filtered on range`() {
        val result = getFilteredVersions {
            it.queryParam("start", "1.0.1").queryParam("stop", "2.0.0-SNAPSHOT")
        }

        expect {
            that(result.responseBody!!.data).map { it.version }.containsExactly("1.0.1")
        }
    }

    fun getFilteredVersions(uriBuilderCustomizer: (UriBuilder) -> UriBuilder): EntityExchangeResult<Page<VersionResource.Responses.Version>> {
        return client.get().uri {
            val builder = it.path(baseUrl)
            uriBuilderCustomizer.invoke(builder)
            builder.build()
        }.exchange()
                .expectStatus().isOk
                .expectBody(ref<Page<VersionResource.Responses.Version>>())
                .returnResult()
    }

    @Test
    fun `Can get single version`() {
        val result = client.get().uri("$baseUrl/${TestData.ns1_schema1_v100}").exchange()
                .expectStatus().isOk
                .expectBody(ref<VersionResource.Responses.Version>())
                .returnResult()

        expectThat(result.responseBody).isEqualTo(VersionResource.Responses.Version(id = TestData.ns1_schema1_v100, schemaId = TestData.ns1_schema1, version = "1.0.0"))
    }

    @Test
    fun `Cannot get unknown single version`() {
        client.get().uri("$baseUrl/${UUID.randomUUID()}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can delete single version`() {
        client.delete().uri("$baseUrl/${TestData.ns1_schema1_v100}").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/${TestData.ns1_schema1_v100}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create version`() {
        val content = VersionResource.Requests.NewVersion(schemaId = TestData.ns1_schema1, version = "2.3.4")
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
        expectThat(result.responseBody).isEqualTo(VersionResource.Responses.Version(id = createdId, schemaId = TestData.ns1_schema1, version = "2.3.4"))
    }

    @Test
    fun `Cannot create duplicate version`() {
        val content = VersionResource.Requests.NewVersion(schemaId = TestData.ns1_schema1, version = "1.0.0")

        client.post().uri(baseUrl)
                .syncBody(content)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}