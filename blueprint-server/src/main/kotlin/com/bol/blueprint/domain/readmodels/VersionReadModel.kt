package com.bol.blueprint.domain.readmodels

import com.bol.blueprint.cqrs.Resettable
import com.bol.blueprint.cqrs.api.EventHandler
import com.bol.blueprint.cqrs.api.EventHandlerBuilder.Companion.eventHandler
import com.bol.blueprint.domain.*
import com.vdurmont.semver4j.Semver
import org.springframework.stereotype.Component

@Component
class VersionReadModel(
    val schemas: SchemaReadModel
) : EventHandler, Resettable {
    data class Entry(
        val namespaceId: NamespaceId,
        val schemaId: SchemaId,
        val versionId: VersionId,
        val version: Version
    )

    private val versions = mutableMapOf<VersionId, Entry>()

    override val eventHandler
        get() = eventHandler {
            handle<VersionCreatedEvent> {
                val namespaceId = schemas.getSchemaNamespaceId(it.schemaId)!!
                val schema = schemas.getSchema(it.schemaId)!!
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

    override fun reset() {
        versions.clear()
    }

    fun getVersions(schemaId: SchemaId) = versions.filter {
        it.value.schemaId == schemaId
    }.map { it.value.version }

    /**
     * Get version based on id
     */
    fun getVersion(versionId: VersionId): Version? = versions[versionId]?.version

    fun getVersionSchemaId(versionId: VersionId): SchemaId? = versions[versionId]?.schemaId

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
}