package com.bol.katalog.api.v1

import com.bol.katalog.cqrs.send
import com.bol.katalog.features.registry.*
import com.bol.katalog.features.registry.support.create
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.support.WithKatalogUser
import com.bol.katalog.support.TestData
import com.bol.katalog.utils.runBlockingAsSystem
import com.vdurmont.semver4j.Semver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import strikt.api.expectThat
import strikt.assertions.contentEquals

@WebFluxTest(RepositoryResource::class)
@WithKatalogUser("user1")
class RepositoryResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/repository"

    private val ar1data = byteArrayOf(1, 2, 3)

    @BeforeEach
    fun before() {
        val ns1 = Namespace("id-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
        val sc1 = Schema("id-sc1", TestData.clock.instant(), "sc1", SchemaType.default(), ns1)
        val ver100 = Version("id-ver100", TestData.clock.instant(), Semver("1.0.0"), sc1)
        val ar1 = Artifact("id-ar1", "ar1.json", ar1data.size, MediaType.JSON, ver100)

        runBlockingAsSystem {
            registry.send(ns1.create())
            registry.send(sc1.create())
            registry.send(ver100.create())
            registry.send(ar1.create(ar1data))
        }
    }

    @Test
    fun `Can get artifact`() {
        val result = exchange<ByteArray>(path = "ns1/sc1/1.0.0/ar1.json")
        expectThat(result!!).contentEquals(ar1data)
    }
}