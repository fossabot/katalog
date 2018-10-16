package com.bol.blueprint.api

import com.bol.blueprint.api.v1.BrowseResource
import com.bol.blueprint.api.v1.Page
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expect
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithUserDetails
class BrowseResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/browse"

    @Test
    fun `Can get browse summary`() {
        val result = client.get().uri(baseUrl).exchange()
                .expectStatus().isOk
                .expectBody(typeReference<Page<BrowseResource.Responses.BrowseNamespace>>())
                .returnResult()

        expect {
            that(result.responseBody!!.data).containsExactly(
                    BrowseResource.Responses.BrowseNamespace(
                            name = "ns1",
                            schemas = listOf(
                                    BrowseResource.Responses.BrowseSchema(
                                            name = "schema1",
                                            versions = listOf(
                                                    BrowseResource.Responses.BrowseVersion(version = "1.0.1"),
                                                    BrowseResource.Responses.BrowseVersion(version = "1.0.0")
                                            )
                                    ),
                                    BrowseResource.Responses.BrowseSchema(
                                            name = "schema2",
                                            versions = emptyList()
                                    )
                            )
                    ),
                    BrowseResource.Responses.BrowseNamespace(
                            name = "ns2",
                            schemas = emptyList()
                    )
            )

            that(result.responseBody!!.totalElements).isEqualTo(2)
        }
    }
}
