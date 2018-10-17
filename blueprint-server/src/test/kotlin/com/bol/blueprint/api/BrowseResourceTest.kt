package com.bol.blueprint.api

import com.bol.blueprint.api.v1.BrowseResource
import com.bol.blueprint.api.v1.Page
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithUserDetails
class BrowseResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/browse"

    @Test
    fun `Can get browse summary`() {
        val result = getBrowseSummary(null)

        expect {
            that(result.responseBody!!.data).containsExactly(ns1(), ns2())
            that(result.responseBody!!.totalElements).isEqualTo(2)
        }
    }

    @Test
    fun `Can filter on namespaces`() {
        val result = getBrowseSummary("ns1")
        expectThat(result.responseBody!!.data).containsExactly(ns1())
    }

    @Test
    fun `Can filter on schemas`() {
        val result = getBrowseSummary("schema2")
        expectThat(result.responseBody!!.data).containsExactly(ns1())
    }

    @Test
    fun `Can filter on non-existent schemas`() {
        val result = getBrowseSummary("FOO")
        expectThat(result.responseBody!!.data).isEmpty()
    }

    private fun getBrowseSummary(filter: String?) = client.get()
            .uri {
                it
                        .path(baseUrl)
                        .queryParam("filter", filter)
                        .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(typeReference<Page<BrowseResource.Responses.BrowseNamespace>>())
            .returnResult()

    private fun ns1() = BrowseResource.Responses.BrowseNamespace(
            name = "ns1",
            schemas = listOf(
                    BrowseResource.Responses.BrowseSchema(
                            name = "schema1",
                            versions = listOf(
                                    BrowseResource.Responses.BrowseVersion(version = "2.0.0"),
                                    BrowseResource.Responses.BrowseVersion(version = "1.0.1")
                            )
                    ),
                    BrowseResource.Responses.BrowseSchema(
                            name = "schema2",
                            versions = emptyList()
                    )
            )
    )

    private fun ns2() = BrowseResource.Responses.BrowseNamespace(
            name = "ns2",
            schemas = emptyList()
    )
}
