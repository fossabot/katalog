package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication
import com.bol.katalog.TestApplication.processor
import com.bol.katalog.TestApplication.registry
import com.bol.katalog.TestData
import com.bol.katalog.security.CoroutineUserContext
import kotlinx.coroutines.runBlocking
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
    fun `Can register namespaces`() {
        runBlocking {
            CoroutineUserContext.set(TestApplication.security.findUserByUsername("user1")!!)
            expectThat(registry.getNamespaces()).containsExactly(
                Namespace("id-ns1", "ns1", "id-group1", TestData.clock.instant()),
                Namespace("id-ns2", "ns2", "id-group1", TestData.clock.instant())
            )
        }
    }

    @Test
    fun `Can delete namespace`() {
        runBlocking {
            CoroutineUserContext.set(TestApplication.security.findUserByUsername("user1")!!)

            processor.apply(DeleteNamespaceCommand("id-ns1"))

            expectThat(registry.getNamespaces()).containsExactly(
                Namespace("id-ns2", "ns2", "id-group1", TestData.clock.instant())
            )
        }
    }

    @Test
    fun `Deleting a namespace should cascade down to artifacts`() {
        runBlocking {
            processor.apply(DeleteNamespaceCommand("id-ns1"))

            expectThat(registry.getSchemas(listOf("id-ns1"))).isEmpty()
            expectThat(registry.getVersions("id-ns1-schema1")).isEmpty()
            expectThat(
                registry.getArtifacts(
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