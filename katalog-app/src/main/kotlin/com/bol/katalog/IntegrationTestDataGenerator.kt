package com.bol.katalog

import com.bol.katalog.config.StartupRunner
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.GroupId
import com.bol.katalog.utils.runBlockingAsSystem
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConditionalOnProperty("katalog.testdata.enabled", matchIfMissing = false)
class IntegrationTestDataGenerator(
    private val registry: Aggregate<RegistryState>
) : StartupRunner {
    override fun runAfterStartup() {
        runBlockingAsSystem {
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
                                        send(
                                            CreateVersionCommand(
                                                schemaId,
                                                versionId,
                                                "$major.$minor.$rev"
                                            )
                                        )

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
