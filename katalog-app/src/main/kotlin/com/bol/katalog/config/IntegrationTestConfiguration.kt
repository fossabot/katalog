package com.bol.katalog.config

import com.bol.katalog.domain.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.*
import javax.annotation.PostConstruct
import kotlin.random.nextInt

@Configuration
@Profile("it")
class IntegrationTestConfiguration {
    @Autowired
    lateinit var commandHandler: Processor

    @PostConstruct
    fun init() {
        val ns1: NamespaceId = UUID.randomUUID()
        val ns2: NamespaceId = UUID.randomUUID()
        val group1: GroupId = UUID.randomUUID()
        val ns1_schema1: SchemaId = UUID.randomUUID()
        val ns1_schema2: SchemaId = UUID.randomUUID()
        val ns2_schema3: SchemaId = UUID.randomUUID()

        runBlocking {
            with(commandHandler) {
                createNamespace(ns1, group1, "ns1")
                createNamespace(ns2, group1, "ns2")

                createSchema(ns1, ns1_schema1, "schema1", SchemaType.default())
                createSchema(ns1, ns1_schema2, "schema2", SchemaType.default())
                createSchema(ns2, ns2_schema3, "schema3", SchemaType.default())

                // Add a huge amount of versions for ns1_schema1
                for (major in 3..20) {
                    val minorCount = kotlin.random.Random.nextInt(10..30)
                    for (minor in 0..minorCount) {
                        val revCount = kotlin.random.Random.nextInt(10..30)
                        for (rev in 0..revCount) {
                            val versionId = UUID.randomUUID()
                            createVersion(ns1_schema1, versionId, "$major.$minor.$rev")

                            createArtifact(
                                versionId,
                                UUID.randomUUID(),
                                "artifact1.json",
                                MediaType.JSON,
                                """{ "hello1": true }""".toByteArray()
                            )
                            createArtifact(
                                versionId,
                                UUID.randomUUID(),
                                "artifact2.json",
                                MediaType.JSON,
                                """{ "hello2": true }""".toByteArray()
                            )
                        }
                    }
                }
            }
        }
    }
}
