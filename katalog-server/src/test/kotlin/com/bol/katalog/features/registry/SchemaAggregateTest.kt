package com.bol.katalog.features.registry

import com.bol.katalog.TestData
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.withTestUser1
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.throws

class SchemaAggregateTest : AbstractAggregateTest() {
    @Test
    fun `Can register schemas`() = withTestUser1 {
        expectThat(schemas.getSchemas(listOf(TestData.ns1))).containsExactly(
            Schema(
                TestData.ns1_schema1,
                TestData.clock.instant(),
                "schema1",
                SchemaType.default()
            ),
            Schema(
                TestData.ns1_schema2,
                TestData.clock.instant(),
                "schema2",
                SchemaType.default()
            )
        )

        expectThat(schemas.getSchemas(listOf(TestData.ns2))).containsExactly(
            Schema(
                TestData.ns2_schema3,
                TestData.clock.instant(),
                "schema3",
                SchemaType.default()
            )
        )
    }

    @Test
    fun `Can find namespaces of schemas`() = withTestUser1 {
        expectThat(schemas.getSchemas(listOf(TestData.ns1)).map { schemas.getSchemaNamespaceId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns1
        )
        expectThat(schemas.getSchemas(listOf(TestData.ns2)).map { schemas.getSchemaNamespaceId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns2
        )
    }

    @Test
    fun `Can delete schema`() = withTestUser1 {
        val schema = schemas.getSchema(TestData.ns1_schema1)

        runBlocking {
            processor.apply(DeleteSchemaCommand(TestData.ns1_schema1))
        }

        expectThat(schemas.getSchemas(listOf(TestData.ns1))).containsExactly(
            Schema(
                TestData.ns1_schema2,
                TestData.clock.instant(),
                "schema2",
                SchemaType.default()
            )
        )

        expectThat(catching { schemas.getSchemaNamespaceId(schema.id) }).throws<NotFoundException>()
    }
}