package com.bol.katalog

import com.bol.katalog.config.StartupRunner
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.GroupId
import com.bol.katalog.utils.runBlockingAsSystem
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*
import kotlin.system.measureTimeMillis

@Component
@ConditionalOnProperty("katalog.testdata.enabled", matchIfMissing = false)
class IntegrationTestDataGenerator(
    private val registry: Aggregate<Registry>
) : StartupRunner {
    private val log = KotlinLogging.logger {}

    override fun runAfterStartup() {
        runBlockingAsSystem {
            var totalSends = 0
            val time = measureTimeMillis {
                with(registry.directAccess()) {
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
                            totalSends++
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
                                totalSends++
                                for (major in 1..3) for (minor in 1..3) for (rev in 0..5) {
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

                                    totalSends += 3
                                }
                            }
                        }
                    }
                }
            }

            val timePerCommand = time.toFloat() / totalSends
            val commandsPerSecond = 1000.0f / timePerCommand
            log.debug("Took $time milliseconds, sent $totalSends commands ($commandsPerSecond commands per second)")
        }
    }
}
