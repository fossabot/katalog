package com.bol.katalog.api.v1

import com.bol.katalog.TestData
import com.bol.katalog.api.AbstractResourceTest
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
    fun `Can get all versions, filtered on current versions`() {
        val result = getFilteredVersions { it.queryParam("onlyCurrentVersions", true) }

        expect {
            that(result.responseBody!!.data.filter { it.schemaId == TestData.ns1_schema1 }).containsExactly(
                VersionResource.Responses.Version(
                    id = TestData.ns1_schema1_v200snapshot,
                    createdOn = TestData.clock.instant(),
                    schemaId = TestData.ns1_schema1,
                    version = "2.0.0-SNAPSHOT",
                    major = 2,
                    stable = false,
                    current = true
                ),
                VersionResource.Responses.Version(
                    id = TestData.ns1_schema1_v101,
                    createdOn = TestData.clock.instant(),
                    schemaId = TestData.ns1_schema1,
                    version = "1.0.1",
                    major = 1,
                    stable = true,
                    current = true
                )
            )

            that(result.responseBody!!.data.filter { it.schemaId == TestData.ns2_schema3 }).containsExactly(
                VersionResource.Responses.Version(
                    id = TestData.ns2_schema3_v100,
                    createdOn = TestData.clock.instant(),
                    schemaId = TestData.ns2_schema3,
                    version = "1.0.0",
                    major = 1,
                    stable = true,
                    current = true
                )
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

    fun getFilteredVersions(uriBuilderCustomizer: (UriBuilder) -> UriBuilder): EntityExchangeResult<PageResponse<VersionResource.Responses.Version>> {
        return client.get().uri {
            val builder = it.path(baseUrl)
            uriBuilderCustomizer.invoke(builder)
            builder.build()
        }.exchange()
            .expectStatus().isOk
            .expectBody(ref<PageResponse<VersionResource.Responses.Version>>())
            .returnResult()
    }

    @Test
    fun `Can get single version`() {
        val result = client.get().uri("$baseUrl/${TestData.ns1_schema1_v100}").exchange()
            .expectStatus().isOk
            .expectBody(ref<VersionResource.Responses.Version>())
            .returnResult()

        expectThat(result.responseBody).isEqualTo(
            VersionResource.Responses.Version(
                id = TestData.ns1_schema1_v100,
                createdOn = TestData.clock.instant(),
                schemaId = TestData.ns1_schema1,
                version = "1.0.0",
                major = 1,
                stable = true,
                current = false
            )
        )
    }

    @Test
    fun `Cannot get unknown single version`() {
        client.get().uri("$baseUrl/${UUID.randomUUID()}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can find single version based on name`() {
        val result = client.get().uri("$baseUrl/find/ns1/schema1/1.0.0").exchange()
            .expectStatus().isOk
            .expectBody(ref<VersionResource.Responses.Version>())
            .returnResult()

        expectThat(result.responseBody).isEqualTo(
            VersionResource.Responses.Version(
                id = TestData.ns1_schema1_v100,
                createdOn = TestData.clock.instant(),
                schemaId = TestData.ns1_schema1,
                version = "1.0.0",
                major = 1,
                stable = true,
                current = false
            )
        )
    }

    @Test
    fun `Cannot find unknown single version based on name`() {
        client.get().uri("$baseUrl/find/unknown").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can delete single version`() {
        client.delete().uri("$baseUrl/${TestData.ns1_schema1_v100}").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/${TestData.ns1_schema1_v100}").exchange().expectStatus().isNotFound
    }

    @Test
    @WithUserDetails("no-groups-user")
    fun `Cannot delete single version with insufficient permissions`() {
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
        expectThat(result.responseBody).isEqualTo(
            VersionResource.Responses.Version(
                id = createdId,
                createdOn = TestData.clock.instant(),
                schemaId = TestData.ns1_schema1,
                version = "2.3.4",
                major = 2,
                stable = true,
                current = true
            )
        )
    }

    @Test
    @WithUserDetails("no-groups-user")
    fun `Cannot create version with insufficient permissions`() {
        val content = VersionResource.Requests.NewVersion(schemaId = TestData.ns1_schema1, version = "2.3.4")
        client.post().uri(baseUrl)
            .syncBody(content)
            .exchange()
            .expectStatus().isNotFound // namespace cannot be found with these permissions
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