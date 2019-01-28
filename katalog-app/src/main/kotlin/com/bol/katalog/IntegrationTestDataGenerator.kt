package com.bol.katalog

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.SecurityState
import com.bol.katalog.security.withUserDetails
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty("katalog.testdata.enabled", matchIfMissing = false)
class IntegrationTestDataGenerator(
    private val aggregateContext: AggregateContext,
    private val security: Aggregate<SecurityState>,
    private val registry: Aggregate<RegistryState>
) {
    @PostConstruct
    fun init() {
        val admin = runBlocking { security.read { findUserByUsername("admin") } }

        runBlocking {
            aggregateContext.onStartup {
                withUserDetails(admin) {
                    with(registry) {
                        for (group in 1..3) {
                            for (namespace in 1..3) {
                                val namespaceId = UUID.randomUUID().toString()
                                send(
                                    CreateNamespaceCommand(
                                        namespaceId,
                                        GroupId("id-group$group"),
                                        "group${group}_ns$namespace"
                                    )
                                )
                                for (schema in 1..3) {
                                    val schemaId = UUID.randomUUID().toString()
                                    send(
                                        CreateSchemaCommand(
                                            namespaceId,
                                            schemaId,
                                            "schema$schema",
                                            SchemaType.default()
                                        )
                                    )
                                    for (major in 1..3) {
                                        for (minor in 1..3) {
                                            for (rev in 0..5) {
                                                val versionId = UUID.randomUUID().toString()
                                                send(CreateVersionCommand(schemaId, versionId, "$major.$minor.$rev"))

                                                send(
                                                    CreateArtifactCommand(
                                                        versionId,
                                                        UUID.randomUUID().toString(),
                                                        "artifact1.json",
                                                        MediaType.JSON,
                                                        """{ "hello1": true }""".toByteArray()
                                                    )
                                                )
                                                send(
                                                    CreateArtifactCommand(
                                                        versionId,
                                                        UUID.randomUUID().toString(),
                                                        "artifact2.json",
                                                        MediaType.JSON,
                                                        """{ "hello2": true }""".toByteArray()
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
