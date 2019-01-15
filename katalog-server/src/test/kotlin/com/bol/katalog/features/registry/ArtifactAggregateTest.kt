package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication
import com.bol.katalog.TestApplication.artifacts
import com.bol.katalog.TestApplication.blobStore
import com.bol.katalog.TestApplication.processor
import com.bol.katalog.cqrs.NotFoundException
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.*

class ArtifactAggregateTest {
    @Before
    fun before() {
        TestApplication.reset()
    }

    @Test
    fun `Can register artifacts`() {
        runBlocking {
            expectThat(artifacts.getArtifacts(listOf("id-ns1-schema1-v100"))).containsExactly(
                Artifact(
                    "id-artifact1",
                    "artifact1.json",
                    3,
                    MediaType.JSON
                )
            )

            expectThat(artifacts.getArtifacts(listOf("id-ns1-schema1-v101"))).containsExactly(
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
        runBlocking {
            expectThat(artifacts.getArtifacts(listOf("id-ns1-schema1-v100")).map { artifacts.getArtifactVersionId(it.id) }.distinct().single()).isEqualTo(
                "id-ns1-schema1-v100"
            )
            expectThat(artifacts.getArtifacts(listOf("id-ns1-schema1-v101")).map { artifacts.getArtifactVersionId(it.id) }.distinct().single()).isEqualTo(
                "id-ns1-schema1-v101"
            )
        }
    }

    @Test
    fun `Can delete artifact`() {
        runBlocking {
            val artifact1 = artifacts.getArtifact("id-artifact1")

            processor.apply(DeleteArtifactCommand("id-artifact1"))

            expectThat(artifacts.getArtifacts(listOf("id-ns1-schema1-v100"))).isEmpty()

            expectThat(blobStore.get(getBlobStorePath("id-artifact1"))).isNull()

            expectThat(catching { artifacts.getArtifactVersionId(artifact1.id) }).throws<NotFoundException>()
        }
    }
}