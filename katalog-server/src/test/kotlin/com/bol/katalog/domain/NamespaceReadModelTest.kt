package com.bol.katalog.domain

import com.bol.katalog.TestData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class NamespaceReadModelTest : AbstractReadModelTest() {
    @Test
    fun `Can register namespaces`() {
        expectThat(namespaces.getNamespaces()).containsExactly(
            Namespace(TestData.ns1, "ns1", TestData.group1),
            Namespace(TestData.ns2, "ns2", TestData.group1)
        )
    }

    @Test
    fun `Can delete namespace`() {
        runBlocking {
            processor.deleteNamespace(TestData.ns1)
        }

        expectThat(namespaces.getNamespaces()).containsExactly(
            Namespace(TestData.ns2, "ns2", TestData.group1)
        )
    }
}