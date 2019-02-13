package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.hazelcast.HazelcastAggregateContext
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

class VersionRegistry(
    context: HazelcastAggregateContext,
    private val permissionManager: PermissionManager,
    private val registry: RegistryAggregate
) {
    private val versions = context.map<VersionId, Version>("registry/v1/versions")
    private val versionsBySchema = context.multiMap<SchemaId, Version>("registry/v1/versions-by-schema")
    private val currentMajorVersions = context.multiMap<SchemaId, Version>("registry/v1/current-major-versions")

    suspend fun getAll(schemaId: SchemaId) = versionsBySchema.read { this[schemaId].orEmpty() }
        .versionsFilteredForUser()
        .asSequence()

    /**
     * Get version based on id
     */
    suspend fun getById(versionId: VersionId): Version {
        val single = versions.read { this[versionId] } ?: throw NotFoundException("Unknown version id: $versionId")
        if (!permissionManager.hasPermissionBy(
                single,
                GroupPermission.READ
            )
        ) throw ForbiddenException("Forbidden to read version: ${single.version}")
        return single
    }

    /**
     * Get the current major versions
     */
    suspend fun getCurrentMajorVersions(schemaId: SchemaId) =
        currentMajorVersions.read { this[schemaId].orEmpty() }
            .asSequence()

    /**
     * Is this a current version (i.e. the latest stable version of a major version)?
     */
    suspend fun isCurrent(version: Version) =
        currentMajorVersions.read { this[version.schemaId].orEmpty() }.contains(version)

    suspend fun getByVersion(schemaId: SchemaId, version: String) =
        versionsBySchema.read { this[schemaId].orEmpty() }
            .versionsFilteredForUser().singleOrNull {
                it.schemaId == schemaId && it.version == version
            }
            ?: throw NotFoundException("Unknown version: $version in schema with id: $schemaId")

    suspend fun exists(schemaId: SchemaId, version: String) = versionsBySchema.read { this[schemaId].orEmpty() }
        .versionsFilteredForUser()
        .any {
            it.version == version
        }

    suspend fun add(version: Version) {
        versions.write { this[version.id] = version }
        versionsBySchema.write { put(version.schemaId, version) }
        updateMajorCurrentVersions(version.schemaId)
    }

    suspend fun removeById(versionId: VersionId) {
        val version = getById(versionId)
        versions.write { remove(versionId) }
        versionsBySchema.write { remove(version.schemaId, version) }
        updateMajorCurrentVersions(version.schemaId)
    }

    private suspend fun Collection<Version>.versionsFilteredForUser() =
        filter { permissionManager.hasPermissionBy(it, GroupPermission.READ) }

    private suspend fun updateMajorCurrentVersions(schemaId: SchemaId) {
        val schema = registry.schemas.getById(schemaId)
        val versions: Collection<Version> = versionsBySchema.read { this[schemaId] ?: emptyList() }

        currentMajorVersions.write {
            val groupedByMajor: Map<Int, List<Version>> = versions
                .map { Pair(it, it.toSemVer(schema)) }
                .sortedByDescending { it.second }
                .groupBy { it.second.major }
                .mapValues { entry -> entry.value.map { it.first } }

            val result = groupedByMajor.mapValues { entry ->
                // Get highest version
                val items = entry.value.sortedByDescending { it.toSemVer(schema) }
                items.first()
            }.map { it.value }

            remove(schemaId)
            result.forEach { put(schemaId, it) }
        }
    }

    fun reset() {
        versions.reset()
        versionsBySchema.reset()
        currentMajorVersions.reset()
    }
}