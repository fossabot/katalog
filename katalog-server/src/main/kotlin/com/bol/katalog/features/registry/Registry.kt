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
    internal val artifactsByVersion: MutableMap<VersionId, MutableList<Artifact>> = context.getMap("registry/v1/artifacts-by-version")
) : State {
    internal val namespaces = NamespaceRegistry(context, permissionManager)
    internal val schemas = SchemaRegistry(context, permissionManager)
    internal val versions = VersionRegistry(context, permissionManager)

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

    private suspend fun Collection<Artifact>.artifactsFilteredForUser() =
        filter { permissionManager.hasPermission(it.version.schema.namespace.groupId, GroupPermission.READ) }
}
