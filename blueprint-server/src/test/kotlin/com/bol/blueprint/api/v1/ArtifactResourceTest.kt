package com.bol.blueprint.api.v1

import com.bol.blueprint.TestData
import com.bol.blueprint.api.AbstractResourceTest
import com.bol.blueprint.domain.MediaType
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.map
import java.net.URI
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithUserDetails
class ArtifactResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/artifacts"

    @Test
    fun `Can get all artifacts`() {
        val result = client.get().uri(baseUrl).exchange()
                .expectStatus().isOk
                .expectBody(ref<Page<ArtifactResource.Responses.Artifact>>())
                .returnResult()

        expect {
            that(result.responseBody!!.data).containsExactly(
                    ArtifactResource.Responses.Artifact(
                            id = TestData.artifact1,
                            versionId = TestData.ns1_schema1_v100,
                            filename = "artifact1.json",
                            mediaType = MediaType.JSON,
                            repositoryPath = URI.create("/api/v1/repository/ns1/schema1/1.0.0/artifact1.json")
                    ),
                    ArtifactResource.Responses.Artifact(
                            id = TestData.artifact2,
                            versionId = TestData.ns1_schema1_v101,
                            filename = "artifact2.json",
                            mediaType = MediaType.JSON,
                            repositoryPath = URI.create("/api/v1/repository/ns1/schema1/1.0.1/artifact2.json")
                    )
            )
        }
    }

    @Test
    fun `Can get filtered artifacts`() {
        val result = client.get().uri {
            it
                    .path(baseUrl)
                    .queryParam("versionIds", TestData.ns1_schema1_v100)
                    .build()
        }.exchange()
                .expectStatus().isOk
                .expectBody(ref<Page<ArtifactResource.Responses.Artifact>>())
                .returnResult()

        expect {
            that(result.responseBody!!.data).map { it.filename }.containsExactly("artifact1.json")
        }
    }

    @Test
    fun `Can get single version`() {
        val result = client.get().uri("$baseUrl/${TestData.artifact1}").exchange()
                .expectStatus().isOk
                .expectBody(ref<ArtifactResource.Responses.Artifact>())
                .returnResult()

        expectThat(result.responseBody).isEqualTo(ArtifactResource.Responses.Artifact(
                id = TestData.artifact1,
                versionId = TestData.ns1_schema1_v100,
                filename = "artifact1.json",
                mediaType = MediaType.JSON,
                repositoryPath = URI.create("/api/v1/repository/ns1/schema1/1.0.0/artifact1.json")
        ))
    }

    @Test
    fun `Can delete single artifact`() {
        client.delete().uri("$baseUrl/${TestData.artifact1}").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/${TestData.artifact1}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Cannot get unknown single artifact`() {
        client.get().uri("$baseUrl/${UUID.randomUUID()}").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create artifact`() {
        val builder = MultipartBodyBuilder()
        builder.part("file", ClassPathResource("api/artifact-example.json"))

        val createdResult = client.post().uri {
            it
                    .path(baseUrl)
                    .queryParam("versionId", TestData.ns1_schema1_v100)
                    .build()
        }
                .syncBody(builder.build())
                .exchange()
                .expectStatus().isCreated
                .expectBody(ref<ArtifactResource.Responses.ArtifactCreated>())
                .returnResult()
        val createdId = createdResult.responseBody!!.id

        val result = client.get().uri("$baseUrl/$createdId")
                .exchange()
                .expectStatus().isOk
                .expectBody(ref<ArtifactResource.Responses.Artifact>())
                .returnResult()
        expectThat(result.responseBody).isEqualTo(ArtifactResource.Responses.Artifact(
                id = createdId,
                versionId = TestData.ns1_schema1_v100,
                filename = "artifact-example.json",
                mediaType = MediaType.JSON,
                repositoryPath = URI.create("/api/v1/repository/ns1/schema1/1.0.0/artifact-example.json")
        ))
    }

    @Test
    fun `Cannot create duplicate artifact`() {
        val builder = MultipartBodyBuilder()
        builder.part("file", ClassPathResource("api/artifact-example.json"))

        client.post().uri {
            it
                    .path(baseUrl)
                    .queryParam("versionId", TestData.ns1_schema1_v100)
                    .build()
        }.syncBody(builder.build()).exchange().expectStatus().isCreated
        client.post().uri {
            it
                    .path(baseUrl)
                    .queryParam("versionId", TestData.ns1_schema1_v100)
                    .build()
        }.syncBody(builder.build()).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}