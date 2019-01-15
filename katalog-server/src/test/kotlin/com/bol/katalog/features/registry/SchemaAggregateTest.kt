package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication
import com.bol.katalog.TestApplication.processor
import com.bol.katalog.TestApplication.registry
import com.bol.katalog.TestData
import com.bol.katalog.cqrs.NotFoundException
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.throws

class SchemaAggregateTest {
    @Before
    fun before() {
        TestApplication.reset()
    }

    @Test
    fun `Can register schemas`() {
        runBlocking {
            expectThat(registry.getSchemas(listOf("id-ns1"))).containsExactly(
                Schema(
                    "id-ns1-schema1",
                    TestData.clock.instant(),
                    "schema1",
                    SchemaType.default()
                ),
                Schema(
                    "id-ns1-schema2",
                    TestData.clock.instant(),
                    "schema2",
                    SchemaType.default()
                )
            )

            expectThat(registry.getSchemas(listOf("id-ns2"))).containsExactly(
                Schema(
                    "id-ns2-schema3",
                    TestData.clock.instant(),
                    "schema3",
                    SchemaType.default()
                )
            )
        }
    }

    @Test
    fun `Can find namespaces of schemas`() {
        runBlocking {
            expectThat(registry.getSchemas(listOf("id-ns1")).map { registry.getSchemaNamespaceId(it.id) }.distinct().single()).isEqualTo(
                "id-ns1"
            )
            expectThat(registry.getSchemas(listOf("id-ns2")).map { registry.getSchemaNamespaceId(it.id) }.distinct().single()).isEqualTo(
                "id-ns2"
            )
        }
    }

    @Test
    fun `Can delete schema`() {
        runBlocking {
            val schema = registry.getSchema("id-ns1-schema1")

            runBlocking {
                processor.apply(DeleteSchemaCommand("id-ns1-schema1"))
            }

            expectThat(registry.getSchemas(listOf("id-ns1"))).containsExactly(
                Schema(
                    "id-ns1-schema2",
                    TestData.clock.instant(),
                    "schema2",
                    SchemaType.default()
                )
            )

            expectThat(catching { registry.getSchemaNamespaceId(schema.id) }).throws<NotFoundException>()
        }
    }
}