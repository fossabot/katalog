package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication
import com.bol.katalog.TestApplication.artifacts
import com.bol.katalog.TestApplication.blobStore
import com.bol.katalog.TestApplication.processor
import com.bol.katalog.TestData
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.withTestUser1
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
    fun `Can register artifacts`() = withTestUser1 {
        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v100))).containsExactly(
            Artifact(
                TestData.artifact1,
                "artifact1.json",
                3,
                MediaType.JSON
            )
        )

        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v101))).containsExactly(
            Artifact(
                TestData.artifact2,
                "artifact2.json",
                3,
                MediaType.JSON
            )
        )

        expectThat(blobStore.get(TestData.artifact1.getBlobStorePath())).isNotNull()
            .contentEquals(byteArrayOf(1, 2, 3))
        expectThat(blobStore.get(TestData.artifact2.getBlobStorePath())).isNotNull()
            .contentEquals(byteArrayOf(4, 5, 6))
    }

    @Test
    fun `Can find versions of artifacts`() = withTestUser1 {
        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v100)).map { artifacts.getArtifactVersionId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns1_schema1_v100
        )
        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v101)).map { artifacts.getArtifactVersionId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns1_schema1_v101
        )
    }

    @Test
    fun `Can delete artifact`() = withTestUser1 {
        val artifact1 = artifacts.getArtifact(TestData.artifact1)

        processor.apply(DeleteArtifactCommand(TestData.artifact1))

        expectThat(artifacts.getArtifacts(listOf(TestData.ns1_schema1_v100))).isEmpty()

        expectThat(blobStore.get(TestData.artifact1.getBlobStorePath())).isNull()

        expectThat(catching { artifacts.getArtifactVersionId(artifact1.id) }).throws<NotFoundException>()
    }
}