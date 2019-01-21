package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication.registry
import com.bol.katalog.TestData
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.readBlocking
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.throws

class SchemaAggregateTest : AbstractAggregateTest() {
    @Test
    fun `Can register schemas`() {
        registry.readBlocking {
            expectThat(getSchemas(listOf("id-ns1"))).containsExactly(
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

            expectThat(getSchemas(listOf("id-ns2"))).containsExactly(
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
        registry.readBlocking {
            expectThat(getSchemas(listOf("id-ns1")).map { getSchemaNamespaceId(it.id) }.distinct().single()).isEqualTo(
                "id-ns1"
            )
            expectThat(getSchemas(listOf("id-ns2")).map { getSchemaNamespaceId(it.id) }.distinct().single()).isEqualTo(
                "id-ns2"
            )
        }
    }

    @Test
    fun `Can delete schema`() {
        val schema = registry.readBlocking { getSchema("id-ns1-schema1") }

        runBlocking {
            registry.send(DeleteSchemaCommand("id-ns1-schema1"))

            registry.read {
                expectThat(getSchemas(listOf("id-ns1"))).containsExactly(
                    Schema(
                        "id-ns1-schema2",
                        TestData.clock.instant(),
                        "schema2",
                        SchemaType.default()
                    )
                )

                expectThat(catching { getSchemaNamespaceId(schema.id) }).throws<NotFoundException>()
            }
        }
    }
}