package com.bol.katalog.api.v1

import com.bol.katalog.TestData
import com.bol.katalog.api.PageResponse
import com.bol.katalog.features.registry.Namespace
import com.bol.katalog.features.registry.Schema
import com.bol.katalog.features.registry.SchemaType
import com.bol.katalog.features.registry.create
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.WithKatalogUser
import com.bol.katalog.utils.runBlockingAs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.map

@WebFluxTest(SchemaResource::class)
@WithKatalogUser("user1")
class SchemaResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/schemas"

    @BeforeEach
    fun before() {
        val ns1 = Namespace("id-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
        val sc1 = Schema("id-sc1", TestData.clock.instant(), "sc1", SchemaType.default(), ns1)
        val sc2 = Schema("id-sc2", TestData.clock.instant(), "sc2", SchemaType.default(), ns1)

        // ns3 belongs to another group, which 'user1' does not have access to
        val ns3 = Namespace("id-ns3", "ns3", GroupId("id-group3"), TestData.clock.instant())
        val sc3 = Schema("id-sc3", TestData.clock.instant(), "sc3", SchemaType.default(), ns3)

        runBlockingAs("id-admin") {
            registry.send(ns1.create())
            registry.send(sc1.create())
            registry.send(sc2.create())

            registry.send(ns3.create())
            registry.send(sc3.create())
        }
    }

    @Test
    fun `Can get multiple`() {
        val result = exchange<PageResponse<SchemaResource.Responses.Schema>>()

        expect {
            that(result!!.data).map { it.schema }.containsExactly("sc1", "sc2")
            that(result.data).map { it.namespace.namespace }.containsExactly("ns1", "ns1")
        }
    }

    @Test
    fun `Can get single`() {
        val result = exchange<SchemaResource.Responses.Schema>(path = "id-sc1")
        expectThat(result).isEqualTo(
            SchemaResource.Responses.Schema(
                id = "id-sc1",
                namespace = SchemaResource.Responses.Schema.Namespace("id-ns1", "ns1"),
                createdOn = TestData.clock.instant(),
                schema = "sc1"
            )
        )
    }

    @Test
    fun `Cannot get single without permission`() {
        exchange(path = "id-sc3", expect = HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Cannot get unknown`() {
        exchange(path = "unknown", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Can find by name`() {
        val result = exchange<SchemaResource.Responses.Schema>(path = "find/ns1/sc1")
        expectThat(result).isEqualTo(
            SchemaResource.Responses.Schema(
                id = "id-sc1",
                namespace = SchemaResource.Responses.Schema.Namespace("id-ns1", "ns1"),
                createdOn = TestData.clock.instant(),
                schema = "sc1"
            )
        )
    }

    @Test
    fun `Cannot find unknown by name`() {
        exchange(path = "find/unknown", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Can delete single`() {
        exchange(method = HttpMethod.DELETE, path = "id-sc1", expect = HttpStatus.NO_CONTENT)
        exchange(method = HttpMethod.DELETE, path = "id-sc1", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot delete with insufficient permissions`() {
        exchange(method = HttpMethod.DELETE, path = "id-sc1", expect = HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Can create`() {
        val content = SchemaResource.Requests.NewSchema(namespaceId = "id-ns1", schema = "scNew")
        val createdResult = exchange<SchemaResource.Responses.SchemaCreated>(
            method = HttpMethod.POST,
            expect = HttpStatus.CREATED,
            body = content
        )
        val createdId = createdResult!!.id

        val result = exchange<SchemaResource.Responses.Schema>(path = createdId)
        expectThat(result).isEqualTo(
            SchemaResource.Responses.Schema(
                id = createdId,
                namespace = SchemaResource.Responses.Schema.Namespace("id-ns1", "ns1"),
                createdOn = TestData.clock.instant(),
                schema = "scNew"
            )
        )
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot create with insufficient permissions`() {
        val content = SchemaResource.Requests.NewSchema(namespaceId = "id-ns1", schema = "scNew")
        exchange(
            method = HttpMethod.POST,
            expect = HttpStatus.FORBIDDEN,
            body = content
        )
    }

    @Test
    fun `Cannot create duplicate`() {
        val content = SchemaResource.Requests.NewSchema(namespaceId = "id-ns1", schema = "sc1")
        exchange(
            method = HttpMethod.POST,
            expect = HttpStatus.CONFLICT,
            body = content
        )
    }
}