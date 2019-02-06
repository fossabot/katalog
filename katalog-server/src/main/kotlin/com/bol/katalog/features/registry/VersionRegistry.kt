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

    suspend fun getAll(schemaId: SchemaId) = versionsBySchema[schemaId].orEmpty()
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
        ) throw ForbiddenException("Forbidden to read version: ${single.version}")
        return single
    }

    /**
     * Get the current major versions
     */
    fun getCurrentMajorVersions(schemaId: SchemaId) = currentMajorVersions[schemaId].orEmpty()
        .asSequence()

    /**
     * Is this a current version (i.e. the latest stable version of a major version)?
     */
    fun isCurrent(version: Version) = currentMajorVersions[version.schema.id].orEmpty().contains(version)

    suspend fun getByVersion(schemaId: SchemaId, version: String) =
        versionsBySchema[schemaId].orEmpty()
            .versionsFilteredForUser().singleOrNull {
                it.schema.id == schemaId && it.version == version
            }
            ?: throw NotFoundException("Unknown version: $version in schema with id: $schemaId")

    suspend fun exists(schemaId: SchemaId, version: String) = versionsBySchema[schemaId].orEmpty()
        .versionsFilteredForUser()
        .any {
            it.version == version
        }

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

    private fun updateMajorCurrentVersions(schemaId: SchemaId) {
        val versions: List<Version> = versionsBySchema[schemaId] ?: emptyList()

        val groupedByMajor: Map<Int, List<Version>> = versions
            .map { Pair(it, it.toSemVer()) }
            .sortedByDescending { it.second }
            .groupBy { it.second.major }
            .mapValues { entry -> entry.value.map { it.first } }

        val result = groupedByMajor.mapValues { entry ->
            // Get highest version
            val items = entry.value.sortedByDescending { it.toSemVer() }
            items.first()
        }.map { it.value }
        if (result.isEmpty()) {
            currentMajorVersions.remove(schemaId)
        } else {
            currentMajorVersions[schemaId] = result
        }
    }
}