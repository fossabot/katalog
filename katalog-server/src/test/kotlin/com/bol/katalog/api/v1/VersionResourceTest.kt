package com.bol.katalog.api.v1

import com.bol.katalog.api.PageResponse
import com.bol.katalog.cqrs.send
import com.bol.katalog.features.registry.Namespace
import com.bol.katalog.features.registry.Schema
import com.bol.katalog.features.registry.SchemaType
import com.bol.katalog.features.registry.Version
import com.bol.katalog.features.registry.support.create
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.support.WithKatalogUser
import com.bol.katalog.support.TestData
import com.bol.katalog.support.ref
import com.bol.katalog.utils.runBlockingAsSystem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.util.UriBuilder
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.map

@WebFluxTest(VersionResource::class)
@WithKatalogUser("user1")
class VersionResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/versions"

    @BeforeEach
    fun before() {
        val ns1 = Namespace("id-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
        val sc1 = Schema("id-sc1", TestData.clock.instant(), "sc1", SchemaType.default(), ns1)
        val ver100 = Version("id-ver100", TestData.clock.instant(), "1.0.0", sc1)
        val ver101 = Version("id-ver101", TestData.clock.instant(), "1.0.1", sc1)
        val ver102 = Version("id-ver102", TestData.clock.instant(), "1.0.2", sc1)

        val sc2 = Schema("id-sc2", TestData.clock.instant(), "sc2", SchemaType.default(), ns1)
        val ver222 = Version("id-ver222", TestData.clock.instant(), "2.2.2", sc2)

        // ns3 belongs to another group, which 'user1' does not have access to
        val ns3 = Namespace("id-ns3", "ns3", GroupId("id-group3"), TestData.clock.instant())
        val sc3 = Schema("id-sc3", TestData.clock.instant(), "sc3", SchemaType.default(), ns3)
        val ver333 = Version("id-ver333", TestData.clock.instant(), "3.3.3", sc3)

        runBlockingAsSystem {
            registry.send(ns1.create())

            registry.send(sc1.create())
            registry.send(ver100.create())
            registry.send(ver101.create())
            registry.send(ver102.create())

            registry.send(sc2.create())
            registry.send(ver222.create())

            registry.send(ns3.create())
            registry.send(sc3.create())
            registry.send(ver333.create())
        }
    }

    @Test
    fun `Can get multiple - Filtered on current versions`() {
        val result = getFilteredVersions {
            queryParam("schemaId", "id-sc1")
            queryParam("onlyCurrentVersions", true)
        }

        expect {
            that(result!!.data).map { it.version }.containsExactly("1.0.2")
            that(result.data).map { it.schemaId }.containsExactly("id-sc1")
        }
    }

    @Test
    fun `Can get multiple - Not filtered on current versions`() {
        val result = getFilteredVersions {
            queryParam("schemaId", "id-sc1")
            queryParam("onlyCurrentVersions", false)
        }

        expect {
            that(result!!.data).map { it.version }.containsExactly("1.0.0", "1.0.1", "1.0.2")
            that(result.data).map { it.schemaId }.containsExactly("id-sc1", "id-sc1", "id-sc1")
        }
    }

    @Test
    fun `Can get multiple - Filtered on schema`() {
        val result = getFilteredVersions { queryParam("schemaId", "id-sc1") }

        expect {
            that(result!!.data).map { it.version }.containsExactly("1.0.2")
        }
    }

    @Test
    fun `Can get multiple - Filtered on range`() {
        val result = getFilteredVersions {
            queryParam("schemaId", "id-sc1")
            queryParam("start", "1.0.0")
            queryParam("stop", "1.0.2")
            queryParam("onlyCurrentVersions", false)
        }

        expect {
            that(result!!.data).map { it.version }.containsExactly("1.0.0", "1.0.1")
        }
    }

    @Test
    fun `Can get single`() {
        val result = exchange<VersionResource.Responses.Version>(path = "id-ver100")
        expectThat(result).isEqualTo(
            VersionResource.Responses.Version(
                id = "id-ver100",
                createdOn = TestData.clock.instant(),
                schemaId = "id-sc1",
                version = "1.0.0",
                major = 1,
                stable = true,
                current = false
            )
        )
    }

    @Test
    fun `Cannot get single without permission`() {
        exchange(path = "id-ver333", expect = HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Cannot get unknown`() {
        exchange(path = "unknown", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Can find by name`() {
        val result = exchange<VersionResource.Responses.Version>(path = "find/ns1/sc1/1.0.0")
        expectThat(result).isEqualTo(
            VersionResource.Responses.Version(
                id = "id-ver100",
                createdOn = TestData.clock.instant(),
                schemaId = "id-sc1",
                version = "1.0.0",
                major = 1,
                stable = true,
                current = false
            )
        )
    }

    @Test
    fun `Cannot find unknown by name`() {
        exchange(path = "find/unknown", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Can delete single`() {
        exchange(method = HttpMethod.DELETE, path = "id-ver100", expect = HttpStatus.NO_CONTENT)
        exchange(method = HttpMethod.DELETE, path = "id-ver100", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot delete with insufficient permissions`() {
        exchange(method = HttpMethod.DELETE, path = "id-ver100", expect = HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Can create`() {
        val content = VersionResource.Requests.NewVersion(schemaId = "id-sc1", version = "2.3.4")
        val createdResult = exchange<VersionResource.Responses.VersionCreated>(
            method = HttpMethod.POST,
            expect = HttpStatus.CREATED,
            body = content
        )
        val createdId = createdResult!!.id

        val result = exchange<VersionResource.Responses.Version>(path = createdId)
        expectThat(result).isEqualTo(
            VersionResource.Responses.Version(
                id = createdId,
                createdOn = TestData.clock.instant(),
                schemaId = "id-sc1",
                version = "2.3.4",
                major = 2,
                stable = true,
                current = true
            )
        )
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot create with insufficient permissions`() {
        val content = VersionResource.Requests.NewVersion(schemaId = "id-sc1", version = "2.3.4")
        exchange(
            method = HttpMethod.POST,
            expect = HttpStatus.FORBIDDEN,
            body = content
        )
    }

    @Test
    fun `Cannot create duplicate`() {
        val content = VersionResource.Requests.NewVersion(schemaId = "id-sc1", version = "1.0.0")
        exchange(
            method = HttpMethod.POST,
            expect = HttpStatus.CONFLICT,
            body = content
        )
    }

    private fun getFilteredVersions(uriBuilderCustomizer: UriBuilder.() -> Unit): PageResponse<VersionResource.Responses.Version>? {
        return client.get().uri {
            val builder = it.path(getBaseUrl())
            uriBuilderCustomizer.invoke(builder)
            builder.build()
        }.exchange()
            .expectStatus().isOk
            .expectBody(ref<PageResponse<VersionResource.Responses.Version>>())
            .returnResult()
            .responseBody
    }
}