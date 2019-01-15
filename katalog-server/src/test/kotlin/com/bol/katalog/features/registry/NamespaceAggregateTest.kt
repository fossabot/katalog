package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication
import com.bol.katalog.TestApplication.artifacts
import com.bol.katalog.TestApplication.namespaces
import com.bol.katalog.TestApplication.processor
import com.bol.katalog.TestApplication.schemas
import com.bol.katalog.TestApplication.versions
import com.bol.katalog.TestData
import com.bol.katalog.withTestUser1
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class NamespaceAggregateTest {
    @Before
    fun before() {
        TestApplication.reset()
    }

    @Test
    fun `Can register namespaces`() = withTestUser1 {
        expectThat(namespaces.getNamespaces()).containsExactly(
            Namespace(TestData.ns1, "ns1", "id-group1", TestData.clock.instant()),
            Namespace(TestData.ns2, "ns2", "id-group1", TestData.clock.instant())
        )
    }

    @Test
    fun `Can delete namespace`() = withTestUser1 {
        processor.apply(DeleteNamespaceCommand(TestData.ns1))

        expectThat(namespaces.getNamespaces()).containsExactly(
            Namespace(TestData.ns2, "ns2", "id-group1", TestData.clock.instant())
        )
    }

    @Test
    fun `Deleting a namespace should cascade down to artifacts`() = withTestUser1 {
        processor.apply(DeleteNamespaceCommand(TestData.ns1))

        expectThat(schemas.getSchemas(listOf(TestData.ns1))).isEmpty()
        expectThat(versions.getVersions(TestData.ns1_schema1)).isEmpty()
        expectThat(
            artifacts.getArtifacts(
                listOf(
                    TestData.ns1_schema1_v100,
                    TestData.ns1_schema1_v101,
                    TestData.ns1_schema1_v200snapshot
                )
            )
        ).isEmpty()
    }
}