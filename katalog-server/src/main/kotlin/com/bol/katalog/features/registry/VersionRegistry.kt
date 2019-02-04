package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

class VersionRegistry(
    val context: AggregateContext,
    private val permissionManager: PermissionManager
) {
    private val versions: MutableMap<VersionId, Version> = context.getMap("registry/v1/versions")
    private val versionsBySchema: MutableMap<SchemaId, MutableList<Version>> =
        context.getMap("registry/v1/versions-by-schema")
    private val currentMajorVersions: MutableMap<SchemaId, List<Version>> =
        context.getMap("registry/v1/major-versions-by-schema")

    suspend fun getAll(schemaId: SchemaId) = getAll(listOf(schemaId))

    suspend fun getAll(schemaIds: List<SchemaId>) = versionsBySchema
        .filterKeys { schemaIds.contains(it) }
        .values
        .flatten()
        .versionsFilteredForUser()
        .asSequence()

    /**
     * Get version based on id
     */
    suspend fun getById(versionId: VersionId): Version {
        val single = versions[versionId] ?: throw NotFoundException("Unknown version id: $versionId")
        if (!permissionManager.hasPermission(
                single.schema.namespace.groupId,
                GroupPermission.READ
            )
        ) throw ForbiddenException("Forbidden to read version: ${single.semVer}")
        return single
    }

    /**
     * Get the current major versions
     */
    fun getCurrentMajorVersions(schemaId: SchemaId) = getCurrentMajorVersions(listOf(schemaId))

    fun getCurrentMajorVersions(schemaIds: List<SchemaId>) = currentMajorVersions
        .filterKeys { schemaIds.contains(it) }
        .values
        .flatten()
        .asSequence()

    /**
     * Is this a current version (i.e. the latest stable version of a major version)?
     */
    fun isCurrent(version: Version) = currentMajorVersions[version.schema.id].orEmpty().contains(version)

    suspend fun getByName(schemaId: SchemaId, version: String) =
        versionsBySchema[schemaId].orEmpty()
            .versionsFilteredForUser().singleOrNull {
                it.schema.id == schemaId && it.semVer.value == version
            }
            ?: throw NotFoundException("Unknown version: $version in schema with id: $schemaId")

    suspend fun exists(schemaId: SchemaId, version: String) = versionsBySchema[schemaId]?.any {
        it.semVer.value == version
    } ?: false

    suspend fun add(version: Version) {
        versions[version.id] = version
        versionsBySchema.getOrPut(version.schema.id) { mutableListOf() }.add(version)
        updateMajorCurrentVersions(version.schema.id)
    }

    suspend fun removeById(versionId: VersionId) {
        val version = getById(versionId)
        versions.remove(versionId)
        versionsBySchema[version.schema.id]!!.remove(version)
        updateMajorCurrentVersions(version.schema.id)
    }

    private suspend fun Collection<Version>.versionsFilteredForUser() =
        filter { permissionManager.hasPermission(it.schema.namespace.groupId, GroupPermission.READ) }

    private suspend fun updateMajorCurrentVersions(schemaId: SchemaId) {
        val result = getAll(schemaId)
            .filter { it.schema.id == schemaId }
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
        if (result.isEmpty()) {
            currentMajorVersions.remove(schemaId)
        } else {
            currentMajorVersions[schemaId] = result
        }
    }
}