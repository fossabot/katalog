package com.bol.katalog.api.v1

import com.bol.katalog.TestData
import com.bol.katalog.api.PageResponse
import com.bol.katalog.features.registry.Namespace
import com.bol.katalog.features.registry.create
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.WithKatalogUser
import kotlinx.coroutines.runBlocking
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

@WebFluxTest(NamespaceResource::class)
@WithKatalogUser("user1")
class NamespaceResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/namespaces"

    @BeforeEach
    fun before() {
        val ns1 = Namespace("id-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
        val ns2 = Namespace("id-ns2", "ns2", GroupId("id-group1"), TestData.clock.instant())

        // ns3 belongs to another group, which 'user1' does not have access to
        val ns3 = Namespace("id-ns3", "ns3", GroupId("id-group3"), TestData.clock.instant())

        runBlocking {
            registry.send(ns1.create())
            registry.send(ns2.create())
            registry.send(ns3.create())
        }
    }

    @Test
    fun `Can get multiple`() {
        val result = exchange<PageResponse<NamespaceResource.Responses.Namespace>>()

        expect {
            that(result!!.data).map { it.namespace }.containsExactly("ns1", "ns2")
        }
    }

    @Test
    fun `Can get single`() {
        val result = exchange<NamespaceResource.Responses.Namespace>(path = "id-ns1")

        expectThat(result).isEqualTo(
            NamespaceResource.Responses.Namespace(
                id = "id-ns1",
                groupId = GroupId("id-group1"),
                namespace = "ns1",
                createdOn = TestData.clock.instant()
            )
        )
    }

    @Test
    fun `Cannot get single without permission`() {
        exchange(path = "id-ns3", expect = HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Cannot get unknown`() {
        exchange(path = "unknown", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Can find by name`() {
        val result = exchange<NamespaceResource.Responses.Namespace>(path = "find/ns1")

        expectThat(result).isEqualTo(
            NamespaceResource.Responses.Namespace(
                id = "id-ns1",
                groupId = GroupId("id-group1"),
                namespace = "ns1",
                createdOn = TestData.clock.instant()
            )
        )
    }

    @Test
    fun `Cannot find unknown by name`() {
        exchange(path = "find/unknown", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Can delete single`() {
        exchange(method = HttpMethod.DELETE, path = "id-ns1", expect = HttpStatus.NO_CONTENT)
        exchange(method = HttpMethod.DELETE, path = "id-ns1", expect = HttpStatus.NOT_FOUND)
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot delete with insufficient permissions`() {
        exchange(method = HttpMethod.DELETE, path = "id-ns1", expect = HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Can create`() {
        val content = NamespaceResource.Requests.NewNamespace(namespace = "foo", groupId = GroupId("id-group1"))
        val createdResult = exchange<NamespaceResource.Responses.NamespaceCreated>(
            method = HttpMethod.POST,
            expect = HttpStatus.CREATED,
            body = content
        )
        val createdId = createdResult!!.id

        val result = exchange<NamespaceResource.Responses.Namespace>(path = createdId)
        expectThat(result).isEqualTo(
            NamespaceResource.Responses.Namespace(
                id = createdId,
                groupId = GroupId("id-group1"),
                namespace = "foo",
                createdOn = TestData.clock.instant()
            )
        )
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Cannot create with insufficient permissions`() {
        val content = NamespaceResource.Requests.NewNamespace(namespace = "foo", groupId = GroupId("id-group1"))
        exchange(
            method = HttpMethod.POST,
            expect = HttpStatus.FORBIDDEN,
            body = content
        )
    }

    @Test
    fun `Cannot create duplicate`() {
        val content = NamespaceResource.Requests.NewNamespace(namespace = "ns1", groupId = GroupId("id-group1"))
        exchange(
            method = HttpMethod.POST,
            expect = HttpStatus.CONFLICT,
            body = content
        )
    }
}