package com.bol.katalog.domain

import com.bol.katalog.TestData
import com.bol.katalog.TestUsers
import com.bol.katalog.security.runBlockingWithUserDetails
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class NamespaceAggregateTest : AbstractAggregateTest() {
    @Test
    fun `Can register namespaces`() {
        val result = runBlockingWithUserDetails(TestUsers.user()) {
            namespaces.getNamespaces()
        }

        expectThat(result).containsExactly(
            Namespace(TestData.ns1, "ns1", Group("group1"), TestData.clock.instant()),
            Namespace(TestData.ns2, "ns2", Group("group1"), TestData.clock.instant())
        )
    }

    @Test
    fun `Can delete namespace`() {
        val result = runBlockingWithUserDetails(TestUsers.user()) {
            processor.deleteNamespace(TestData.ns1)

            namespaces.getNamespaces()
        }

        expectThat(result).containsExactly(
            Namespace(TestData.ns2, "ns2", Group("group1"), TestData.clock.instant())
        )
    }

    @Test
    fun `Deleting a namespace should cascade down to artifacts`() {
        runBlockingWithUserDetails(TestUsers.user()) {
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
}