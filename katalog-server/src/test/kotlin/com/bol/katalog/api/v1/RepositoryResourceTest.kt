package com.bol.katalog.api.v1

import com.bol.katalog.api.AbstractResourceTest
import com.bol.katalog.cqrs.sendLocal
import com.bol.katalog.features.registry.*
import com.bol.katalog.features.registry.support.create
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.support.WithKatalogUser
import com.bol.katalog.testing.TestData
import com.bol.katalog.utils.runBlockingAsSystem
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import strikt.api.expectThat
import strikt.assertions.contentEquals

@WebFluxTest(RepositoryResource::class)
@WithKatalogUser("user1")
class RepositoryResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/repository"

    private val ar1data = byteArrayOf(1, 2, 3)

    @Test
    fun `Can get artifact`() {
        val ns1 = Namespace("id-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
        val sc1 = Schema("id-sc1", ns1.groupId, ns1.id, TestData.clock.instant(), "sc1", SchemaType.default())
        val ver100 = Version("id-ver100", ns1.groupId, sc1.id, TestData.clock.instant(), "1.0.0")
        val ar1 = Artifact("id-ar1", ns1.groupId, ver100.id, "ar1.json", ar1data.size, MediaType.JSON)

        runBlockingAsSystem {
            context.sendLocal(
                ns1.create(),
                sc1.create(),
                ver100.create(),
                ar1.create(ar1data)
            )
        }

        val result = exchange<ByteArray>(path = "ns1/sc1/1.0.0/ar1.json")
        expectThat(result!!).contentEquals(ar1data)
    }
}