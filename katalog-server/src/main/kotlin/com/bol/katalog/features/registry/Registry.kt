package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.State
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

data class Registry(
    val context: AggregateContext,
    private val permissionManager: PermissionManager,

    internal val artifacts: MutableMap<ArtifactId, Artifact> = context.getMap("registry/v1/artifacts"),
    internal val artifactsByVersion: MutableMap<VersionId, MutableList<Artifact>> = context.getMap("registry/v1/artifacts-by-version"),

    internal val versions: MutableMap<VersionId, Version> = context.getMap("registry/v1/versions"),
    internal val versionsBySchema: MutableMap<SchemaId, MutableList<Version>> = context.getMap("registry/v1/versions-by-schema"),
    internal val currentMajorVersions: MutableMap<SchemaId, List<Version>> = context.getMap("registry/v1/major-versions-by-schema")
) : State {
    internal val namespaces = NamespaceRegistry(context, permissionManager)
    internal val schemas = SchemaRegistry(context, permissionManager)

    suspend fun getVersions(schemaId: SchemaId) = versionsBySchema[schemaId].orEmpty().versionsFilteredForUser()

    /**
     * Get version based on id
     */
    suspend fun getVersion(versionId: VersionId): Version {
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
    fun getCurrentMajorVersions(schemaId: SchemaId) = currentMajorVersions[schemaId].orEmpty()

    /**
     * Is this a current version (i.e. the latest stable version of a major version)?
     */
    fun isCurrent(version: Version) = currentMajorVersions[version.schema.id].orEmpty().contains(version)

    suspend fun findVersion(namespaceId: NamespaceId, schemaId: SchemaId, version: String) =
        versionsBySchema[schemaId].orEmpty()
            .versionsFilteredForUser().singleOrNull {
                it.schema.namespace.id == namespaceId && it.schema.id == schemaId && it.semVer.value == version
            }
            ?: throw NotFoundException("Unknown version: $version in schema with id: $schemaId and namespace with id: $namespaceId")

    suspend fun getArtifacts(versionIds: Collection<VersionId>) = artifactsByVersion
        .filterKeys { versionIds.contains(it) }
        .values
        .flatten()
        .artifactsFilteredForUser()

    /**
     * Get artifact based on id
     */
    suspend fun getArtifact(artifactId: ArtifactId): Artifact {
        val single = artifacts[artifactId] ?: throw NotFoundException("Unknown artifact id: $artifactId")
        if (!permissionManager.hasPermission(
                single.version.schema.namespace.groupId,
                GroupPermission.READ
            )
        ) throw ForbiddenException("Forbidden to read artifact: ${single.filename}")
        return single
    }

    suspend fun findArtifact(
        namespaceId: NamespaceId,
        schemaId: SchemaId,
        versionId: VersionId,
        filename: String
    ): Artifact {
        return artifactsByVersion[versionId].orEmpty()
            .artifactsFilteredForUser()
            .singleOrNull {
                it.version.schema.namespace.id == namespaceId && it.version.schema.id == schemaId
                        && it.version.id == versionId && it.filename == filename
            }
            ?: throw NotFoundException("Unknown artifact: $filename in version $versionId in schema with id: $schemaId and namespace with id: $namespaceId")
    }

    private suspend fun Collection<Version>.versionsFilteredForUser() =
        filter { permissionManager.hasPermission(it.schema.namespace.groupId, GroupPermission.READ) }

    private suspend fun Collection<Artifact>.artifactsFilteredForUser() =
        filter { permissionManager.hasPermission(it.version.schema.namespace.groupId, GroupPermission.READ) }

    suspend fun updateMajorCurrentVersions(schemaId: SchemaId) {
        val result = getVersions(schemaId)
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
