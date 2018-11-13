package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

class ArtifactReadModelTest : AbstractReadModelTest() {

    @Test
    fun `Can register artifacts`() {
        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v100))).containsExactly(
            Artifact(
                TestData.artifact1,
                "artifact1.json",
                MediaType.JSON
            )
        )

        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v101))).containsExactly(
            Artifact(
                TestData.artifact2,
                "artifact2.json",
                MediaType.JSON
            )
        )

        runBlocking {
            expectThat(blobStore.get(TestData.artifact1.getBlobStorePath())).isNotNull()
                .contentEquals(byteArrayOf(1, 2, 3))
            expectThat(blobStore.get(TestData.artifact2.getBlobStorePath())).isNotNull()
                .contentEquals(byteArrayOf(4, 5, 6))
        }
    }

    @Test
    fun `Can find versions of artifacts`() {
        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v100)).map { artifacts.getArtifactVersionId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns1_schema1_v100
        )
        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v101)).map { artifacts.getArtifactVersionId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns1_schema1_v101
        )
    }

    @Test
    fun `Can delete artifact`() {
        val artifact1 = artifacts.getArtifact(TestData.artifact1)!!

        runBlocking {
            processor.deleteArtifact(TestData.artifact1)
        }

        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v100))).isEmpty()

        runBlocking {
            expectThat(blobStore.get(TestData.artifact1.getBlobStorePath())).isNull()
        }

        expectThat(artifacts.getArtifactVersionId(artifact1.id)).isNull()
    }
}