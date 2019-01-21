package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication.blobStore
import com.bol.katalog.TestApplication.registry
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.readBlocking
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.*

class ArtifactAggregateTest : AbstractAggregateTest() {
    @Test
    fun `Can register artifacts`() {
        registry.readBlocking {
            expectThat(getArtifacts(listOf("id-ns1-schema1-v100"))).containsExactly(
                Artifact(
                    "id-artifact1",
                    "artifact1.json",
                    3,
                    MediaType.JSON
                )
            )

            expectThat(getArtifacts(listOf("id-ns1-schema1-v101"))).containsExactly(
                Artifact(
                    "id-artifact2",
                    "artifact2.json",
                    3,
                    MediaType.JSON
                )
            )

            expectThat(blobStore.get(getBlobStorePath("id-artifact1"))).isNotNull()
                .contentEquals(byteArrayOf(1, 2, 3))
            expectThat(blobStore.get(getBlobStorePath("id-artifact2"))).isNotNull()
                .contentEquals(byteArrayOf(4, 5, 6))
        }
    }

    @Test
    fun `Can find versions of artifacts`() {
        registry.readBlocking {
            expectThat(getArtifacts(listOf("id-ns1-schema1-v100")).map { getArtifactVersionId(it.id) }.distinct().single()).isEqualTo(
                "id-ns1-schema1-v100"
            )
            expectThat(getArtifacts(listOf("id-ns1-schema1-v101")).map { getArtifactVersionId(it.id) }.distinct().single()).isEqualTo(
                "id-ns1-schema1-v101"
            )
        }
    }

    @Test
    fun `Can delete artifact`() {
        val artifact1 = registry.readBlocking { getArtifact("id-artifact1") }

        runBlocking { registry.send(DeleteArtifactCommand("id-artifact1")) }

        registry.readBlocking {
            expectThat(getArtifacts(listOf("id-ns1-schema1-v100"))).isEmpty()

            expectThat(blobStore.get(getBlobStorePath("id-artifact1"))).isNull()

            expectThat(catching { getArtifactVersionId(artifact1.id) }).throws<NotFoundException>()
        }
    }
}