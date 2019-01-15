package com.bol.katalog.api.v1

import com.bol.katalog.AbstractSpringTest
import com.bol.katalog.api.PageResponse
import com.bol.katalog.features.registry.MediaType
import com.bol.katalog.security.WithKatalogUser
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.client.MultipartBodyBuilder
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.map
import java.net.URI

@WithKatalogUser("user1")
class ArtifactResourceTest : AbstractSpringTest() {
    private val baseUrl = "/api/v1/artifacts"

    @Test
    fun `Can get all artifacts`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<PageResponse<ArtifactResource.Responses.Artifact>>())
            .returnResult()

        expect {
            that(result.responseBody!!.data).containsExactly(
                ArtifactResource.Responses.Artifact(
                    id = "id-artifact1",
                    versionId = "id-ns1-schema1-v100",
                    filename = "artifact1.json",
                    filesize = 3,
                    mediaType = MediaType.JSON,
                    repositoryPath = URI.create("/api/v1/repository/ns1/schema1/1.0.0/artifact1.json")
                ),
                ArtifactResource.Responses.Artifact(
                    id = "id-artifact2",
                    versionId = "id-ns1-schema1-v101",
                    filename = "artifact2.json",
                    filesize = 3,
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
                .queryParam("versionIds", "id-ns1-schema1-v100")
                .build()
        }.exchange()
            .expectStatus().isOk
            .expectBody(ref<PageResponse<ArtifactResource.Responses.Artifact>>())
            .returnResult()

        expect {
            that(result.responseBody!!.data).map { it.filename }.containsExactly("artifact1.json")
        }
    }

    @Test
    fun `Can get single artifact`() {
        val result = client.get().uri("$baseUrl/id-artifact1").exchange()
            .expectStatus().isOk
            .expectBody(ref<ArtifactResource.Responses.Artifact>())
            .returnResult()

        expectThat(result.responseBody).isEqualTo(
            ArtifactResource.Responses.Artifact(
                id = "id-artifact1",
                versionId = "id-ns1-schema1-v100",
                filename = "artifact1.json",
                filesize = 3,
                mediaType = MediaType.JSON,
                repositoryPath = URI.create("/api/v1/repository/ns1/schema1/1.0.0/artifact1.json")
            )
        )
    }

    @Test
    fun `Can delete single artifact`() {
        client.delete().uri("$baseUrl/id-artifact1").exchange().expectStatus().isNoContent
        client.delete().uri("$baseUrl/id-artifact1").exchange().expectStatus().isNotFound
    }

    @Test
    @WithKatalogUser("no-groups-user")
    fun `Cannot delete single artifact with insufficient permissions`() {
        client.delete().uri("$baseUrl/id-artifact1").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Cannot get unknown single artifact`() {
        client.get().uri("$baseUrl/id-unknown").exchange().expectStatus().isNotFound
    }

    @Test
    fun `Can create artifact`() {
        val exampleResource = ClassPathResource("api/artifact-example.json")

        val builder = MultipartBodyBuilder()
        builder.part("file", exampleResource)

        val createdResult = client.post().uri {
            it
                .path(baseUrl)
                .queryParam("versionId", "id-ns1-schema1-v100")
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
        expectThat(result.responseBody).isEqualTo(
            ArtifactResource.Responses.Artifact(
                id = createdId,
                versionId = "id-ns1-schema1-v100",
                filename = "artifact-example.json",
                filesize = exampleResource.contentLength().toInt(),
                mediaType = MediaType.JSON,
                repositoryPath = URI.create("/api/v1/repository/ns1/schema1/1.0.0/artifact-example.json")
            )
        )
    }

    @Test
    @WithKatalogUser("no-groups-user")
    fun `Cannot create artifact with insufficient permissions`() {
        val exampleResource = ClassPathResource("api/artifact-example.json")

        val builder = MultipartBodyBuilder()
        builder.part("file", exampleResource)

        client.post().uri {
            it
                .path(baseUrl)
                .queryParam("versionId", "id-ns1-schema1-v100")
                .build()
        }
            .syncBody(builder.build())
            .exchange()
            .expectStatus().isNotFound // namespace cannot be found with these permissions
    }

    @Test
    fun `Cannot create duplicate artifact`() {
        val builder = MultipartBodyBuilder()
        builder.part("file", ClassPathResource("api/artifact-example.json"))

        client.post().uri {
            it
                .path(baseUrl)
                .queryParam("versionId", "id-ns1-schema1-v100")
                .build()
        }.syncBody(builder.build()).exchange().expectStatus().isCreated
        client.post().uri {
            it
                .path(baseUrl)
                .queryParam("versionId", "id-ns1-schema1-v100")
                .build()
        }.syncBody(builder.build()).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}