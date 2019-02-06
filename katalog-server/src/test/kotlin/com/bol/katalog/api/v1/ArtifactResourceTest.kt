package com.bol.katalog.api.v1

import com.bol.katalog.api.PageResponse
import com.bol.katalog.cqrs.send
import com.bol.katalog.features.registry.*
import com.bol.katalog.features.registry.support.create
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.support.WithKatalogUser
import com.bol.katalog.support.TestData
import com.bol.katalog.support.ref
import com.bol.katalog.utils.runBlockingAsSystem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.MultipartBodyBuilder
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import java.net.URI

@WebFluxTest(ArtifactResource::class)
@WithKatalogUser("user1")
class ArtifactResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/artifacts"

    private val ar1data = byteArrayOf(1, 2, 3)
    private val ar2data = byteArrayOf(4, 5, 6, 7)

    @BeforeEach
    fun before() {
        val ns1 = Namespace("id-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
        val sc1 = Schema("id-sc1", TestData.clock.instant(), "sc1", SchemaType.default(), ns1)
        val ver100 = Version("id-ver100", TestData.clock.instant(), "1.0.0", sc1)
        val ar1 = Artifact("id-ar1", "ar1.json", ar1data.size, MediaType.JSON, ver100)
        val ar2 = Artifact("id-ar2", "ar2.xml", ar2data.size, MediaType.XML, ver100)

        // ns3 belongs to another group, which 'user1' does not have access to
        val ns3 = Namespace("id-ns3", "ns3", GroupId("id-group2"), TestData.clock.instant())
        val sc3 = Schema("id-sc3", TestData.clock.instant(), "sc3", SchemaType.default(), ns3)
        val ver333 = Version("id-ver333", TestData.clock.instant(), "3.3.3", sc3)
        val ar3 = Artifact("id-ar3", "ar3.xml", 0, MediaType.XML, ver333)

        runBlockingAsSystem {
            registry.send(ns1.create())
            registry.send(sc1.create())
            registry.send(ver100.create())
            registry.send(ar1.create(ar1data))
            registry.send(ar2.create(ar2data))

            registry.send(ns3.create())
            registry.send(sc3.create())
            registry.send(ver333.create())
            registry.send(ar3.create(byteArrayOf()))
        }
    }

    @Test
    fun `Can get multiple`() {
        val result =
            exchange<PageResponse<ArtifactResource.Responses.Artifact>>(queryParams = mapOf("versionIds" to "id-ver100"))

        expect {
            that(result!!.data).containsExactly(
                ArtifactResource.Responses.Artifact(
                    id = "id-ar1",
                    versionId = "id-ver100",
                    filename = "ar1.json",
                    filesize = ar1data.size,
                    mediaType = MediaType.JSON,
                    repositoryPath = URI.create("/api/v1/repository/ns1/sc1/1.0.0/ar1.json")
                ),
                ArtifactResource.Responses.Artifact(
                    id = "id-ar2",
                    versionId = "id-ver100",
                    filename = "ar2.xml",
                    filesize = ar2data.size,
                    mediaType = MediaType.XML,
                    repositoryPath = URI.create("/api/v1/repository/ns1/sc1/1.0.0/ar2.xml")
                )
            )
        }
    }

    @Test
    fun `Can get single`() {
        val result = exchange<ArtifactResource.Responses.Artifact>(path = "id-ar1")
        expectThat(result).isEqualTo(
            ArtifactResource.Responses.Artifact(
                id = "id-ar1",
                versionId = "id-ver100",
                filename = "ar1.json",
                filesize = ar1data.size,
                mediaType = MediaType.JSON,
                repositoryPath = URI.create("/api/v1/repository/ns1/sc1/1.0.0/ar1.json")
            )
        )
    }

    @Test
    fun `Cannot get single without permission`() {
        exchange(path = "id-ar3", expect = HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Cannot get unknown`() {
        exchange(path = "unknown", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Can delete single`() {
        exchange(method = HttpMethod.DELETE, path = "id-ar1", expect = HttpStatus.NO_CONTENT)
        exchange(method = HttpMethod.DELETE, path = "id-ar1", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot delete with insufficient permissions`() {
        exchange(method = HttpMethod.DELETE, path = "id-ar1", expect = HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Can create`() {
        val exampleResource = ClassPathResource("api/artifact-example.json")

        val builder = MultipartBodyBuilder()
        builder.part("file", exampleResource)

        val createdResult = client.post().uri {
            it
                .path(getBaseUrl())
                .queryParam("versionId", "id-ver100")
                .build()
        }
            .syncBody(builder.build())
            .exchange()
            .expectStatus().isCreated
            .expectBody(ref<ArtifactResource.Responses.ArtifactCreated>())
            .returnResult()
        val createdId = createdResult.responseBody!!.id

        val result = exchange<ArtifactResource.Responses.Artifact>(path = createdId)
        expectThat(result).isEqualTo(
            ArtifactResource.Responses.Artifact(
                id = createdId,
                versionId = "id-ver100",
                filename = "artifact-example.json",
                filesize = exampleResource.contentLength().toInt(),
                mediaType = MediaType.JSON,
                repositoryPath = URI.create("/api/v1/repository/ns1/sc1/1.0.0/artifact-example.json")
            )
        )
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot create with insufficient permissions`() {
        val exampleResource = ClassPathResource("api/artifact-example.json")

        val builder = MultipartBodyBuilder()
        builder.part("file", exampleResource)

        client.post().uri {
            it
                .path(getBaseUrl())
                .queryParam("versionId", "id-ver100")
                .build()
        }
            .syncBody(builder.build())
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `Cannot create duplicate`() {
        val exampleResource = ClassPathResource("api/artifact-example.json")

        val builder = MultipartBodyBuilder()
        builder.part("file", exampleResource)

        client.post().uri {
            it
                .path(getBaseUrl())
                .queryParam("versionId", "id-ver100")
                .build()
        }
            .syncBody(builder.build())
            .exchange()
            .expectStatus().isCreated
        client.post().uri {
            it
                .path(getBaseUrl())
                .queryParam("versionId", "id-ver100")
                .build()
        }.syncBody(builder.build()).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}