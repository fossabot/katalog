package com.bol.katalog.domain

import com.bol.katalog.TestData
import com.bol.katalog.withTestUser
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class NamespaceAggregateTest : AbstractAggregateTest() {
    @Test
    fun `Can register namespaces`() = withTestUser {
        expectThat(namespaces.getNamespaces()).containsExactly(
            Namespace(TestData.ns1, "ns1", Group("group1"), TestData.clock.instant()),
            Namespace(TestData.ns2, "ns2", Group("group1"), TestData.clock.instant())
        )
    }

    @Test
    fun `Can delete namespace`() = withTestUser {
        processor.deleteNamespace(TestData.ns1)

        expectThat(namespaces.getNamespaces()).containsExactly(
            Namespace(TestData.ns2, "ns2", Group("group1"), TestData.clock.instant())
        )
    }

    @Test
    fun `Deleting a namespace should cascade down to artifacts`() = withTestUser {
        processor.deleteNamespace(TestData.ns1)

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