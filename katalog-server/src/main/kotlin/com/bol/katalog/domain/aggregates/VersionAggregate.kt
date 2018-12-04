package com.bol.katalog.domain.aggregates

import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.CommandHandlerBuilder.Companion.handleCommands
import com.bol.katalog.cqrs.events.EventHandler
import com.bol.katalog.cqrs.events.EventHandlerBuilder.Companion.handleEvents
import com.bol.katalog.domain.*
import com.vdurmont.semver4j.Semver
import org.springframework.stereotype.Component

@Component
class VersionAggregate(
    private val schemas: SchemaAggregate
) : EventHandler, CommandHandler, Resettable {
    data class Entry(
        val namespaceId: NamespaceId,
        val schemaId: SchemaId,
        val versionId: VersionId,
        val version: Version
    )

    private val versions = mutableMapOf<VersionId, Entry>()

    override val eventHandler
        get() = handleEvents {
            handle<VersionCreatedEvent> {
                val namespaceId = schemas.getSchemaNamespaceId(it.schemaId)
                val schema = schemas.getSchema(it.schemaId)
                val version = Version(
                    it.id,
                    metadata.timestamp,
                    Semver(it.version, schema.type.toSemVerType())
                )
                versions[it.id] =
                        Entry(namespaceId, it.schemaId, it.id, version)
            }
            handle<VersionDeletedEvent> {
                versions.remove(it.id)
            }
        }

    override val commandHandler
        get() = handleCommands {
            handle<CreateVersionCommand> {
                if (versions.values.any {
                        it.schemaId == command.schemaId && it.version.semVer.value == command.version
                    }) throw ConflictException()

                event(VersionCreatedEvent(command.schemaId, command.id, command.version))
                complete()
            }

            handle<DeleteVersionCommand> {
                if (!versions.containsKey(command.id)) throw NotFoundException()

                event(VersionDeletedEvent(command.id))
                complete()
            }
        }

    override fun reset() {
        versions.clear()
    }

    fun getVersions(schemaId: SchemaId) = versions.filter {
        it.value.schemaId == schemaId
    }.map { it.value.version }

    /**
     * Get version based on id
     */
    fun getVersion(versionId: VersionId) =
        versions[versionId]?.version ?: throw NotFoundException("Could not find version with id: $versionId")

    fun getVersionSchemaId(versionId: VersionId) = versions[versionId]?.schemaId
        ?: throw NotFoundException("Could not find version with id: $versionId")

    /**
     * Get the current major versions
     */
    fun getCurrentMajorVersions(versions: Collection<Version>): Collection<Version> {
        return versions
            .sortedByDescending { it.semVer }
            .groupBy { it.semVer.major }
            .mapValues { entry ->
                val items = entry.value
                if (items.size == 1) {
                    items
                } else {
                    // Find first stable version
                    val stableVersion = items.first { it.semVer.isStable }
                    listOf(stableVersion)
                }
            }
            .flatMap { it.value }
    }

    /**
     * Is this a current version (i.e. the latest stable version of a major version)?
     */
    fun isCurrent(schemaId: SchemaId, version: Version) =
        getCurrentMajorVersions(getVersions(schemaId)).contains(version)

    fun findVersion(namespaceId: NamespaceId, schemaId: SchemaId, version: String) = versions.values
        .filter {
            it.namespaceId == namespaceId && it.schemaId == schemaId && it.version.semVer.value == version
        }
        .map { it.version }
        .singleOrNull()
        ?: throw NotFoundException("Could not find version: $version in schema with id: $schemaId and namespace with id: $namespaceId")
}