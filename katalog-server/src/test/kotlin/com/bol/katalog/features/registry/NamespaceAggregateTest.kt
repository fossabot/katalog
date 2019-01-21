package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication.registry
import com.bol.katalog.TestData
import com.bol.katalog.readBlocking
import com.bol.katalog.sendBlocking
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class NamespaceAggregateTest : AbstractAggregateTest() {
    @Test
    fun `Can register namespaces`() {
        registry.readBlocking("user1") {
            expectThat(getNamespaces()).containsExactly(
                Namespace("id-ns1", "ns1", "id-group1", TestData.clock.instant()),
                Namespace("id-ns2", "ns2", "id-group1", TestData.clock.instant())
            )
        }
    }

    @Test
    fun `Can delete namespace`() {
        registry.sendBlocking("user1", DeleteNamespaceCommand("id-ns1"))

        registry.readBlocking("user1") {
            expectThat(getNamespaces()).containsExactly(
                Namespace("id-ns2", "ns2", "id-group1", TestData.clock.instant())
            )
        }
    }

    @Test
    fun `Deleting a namespace should cascade down to artifacts`() {
        registry.sendBlocking("user1", DeleteNamespaceCommand("id-ns1"))

        registry.readBlocking("user1") {
            expectThat(getSchemas(listOf("id-ns1"))).isEmpty()
            expectThat(getVersions("id-ns1-schema1")).isEmpty()
            expectThat(
                getArtifacts(
                    listOf(
                        "id-ns1-schema1-v100",
                        "id-ns1-schema1-v101",
                        "id-ns1-schema1-v200snapshot"
                    )
                )
            ).isEmpty()
        }
    }
}