package com.bol.blueprint

import kotlinx.coroutines.experimental.launch
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class DomainTest {
    companion object : KLogging() {
        val NS1 = NamespaceKey("ns1")
        val NS2 = NamespaceKey("ns2")
        val SCHEMA1 = SchemaKey("ns1", "schema1")
        val SCHEMA2 = SchemaKey("ns1", "schema2")
        val VERSION1 = VersionKey("ns1", "schema1", "1.0.0")
    }

    private lateinit var dispatcher: Dispatcher
    private lateinit var query: Query

    @Before
    fun before() {
        dispatcher = Dispatcher()
        query = Query()
        dispatcher.addListener(query)
    }

    @Test
    fun `can register namespaces`() {
        launch {
            dispatcher.createNamespace(NS1)
            dispatcher.createNamespace(NS2)
        }

        await().untilAsserted {
            assertThat(query.getNamespaces()).containsExactlyInAnyOrder(
                Namespace("ns1"),
                Namespace("ns2")
            )
        }
    }

    @Test
    fun `can register schemas`() {
        launch {
            dispatcher.createNamespace(NS1)
            dispatcher.createSchema(SCHEMA1, SchemaType.default())
            dispatcher.createSchema(SCHEMA2, SchemaType.default())
        }

        await().untilAsserted {
            assertThat(query.getSchemas(NS1)).containsExactlyInAnyOrder(
                Schema("schema1", SchemaType.default()),
                Schema("schema2", SchemaType.default())
            )
        }
    }

    @Test
    fun `can register versions`() {
        launch {
            dispatcher.createNamespace(NS1)
            dispatcher.createSchema(SCHEMA1, SchemaType.default())
            dispatcher.createVersion(VERSION1)
        }

        await().untilAsserted {
            assertThat(query.getVersions(SCHEMA1)).containsExactlyInAnyOrder(
                Version("1.0.0")
            )
        }
    }
}