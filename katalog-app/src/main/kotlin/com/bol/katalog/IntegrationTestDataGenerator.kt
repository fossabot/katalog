package com.bol.katalog

import com.bol.katalog.config.StartupRunner
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.SystemUser
import com.bol.katalog.utils.runBlockingAsSystem
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*
import kotlin.system.measureTimeMillis

@Component
@ConditionalOnProperty("katalog.testdata.enabled", matchIfMissing = false)
class IntegrationTestDataGenerator(
    private val context: AggregateContext
) : StartupRunner {
    private val log = KotlinLogging.logger {}

    private val commands = mutableListOf<Command>()

    override fun runAfterStartup() {
        runBlockingAsSystem {
            val time = measureTimeMillis {
                for (group in 1..3) {
                    createGroup(group)
                }

                context.sendLocalAs(SystemUser.get().id, commands)
            }

            val timePerCommand = time.toFloat() / commands.size
            val commandsPerSecond = 1000.0f / timePerCommand
            log.info("Took $time milliseconds, sent ${commands.size} commands ($commandsPerSecond commands per second)")
        }
    }

    private fun createGroup(group: Int) {
        for (namespace in 1..3) {
            createNamespace(group, namespace)
        }
    }

    private fun createNamespace(group: Int, namespace: Int) {
        val namespaceId = UUID.randomUUID().toString()
        commands.add(
            CreateNamespaceCommand(
                namespaceId,
                GroupId("id-group$group"),
                "group${group}_ns$namespace"
            )
        )
        for (schema in 1..3) {
            createSchema(namespaceId, schema)
        }
    }

    private fun createSchema(namespaceId: NamespaceId, schema: Int) {
        val schemaId = UUID.randomUUID().toString()
        commands.add(
            CreateSchemaCommand(
                namespaceId,
                schemaId,
                "schema$schema",
                SchemaType.default()
            )
        )
        for (major in 1..3) for (minor in 1..3) for (rev in 0..5) {
            val versionId = UUID.randomUUID().toString()
            commands.add(
                CreateVersionCommand(
                    schemaId,
                    versionId,
                    "$major.$minor.$rev"
                )
            )

            commands.add(
                CreateArtifactCommand(
                    versionId,
                    UUID.randomUUID().toString(),
                    "artifact1.json",
                    MediaType.JSON,
                    """{ "hello1": true }""".toByteArray()
                )
            )
            commands.add(
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
