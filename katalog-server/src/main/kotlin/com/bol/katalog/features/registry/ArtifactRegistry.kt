package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

class ArtifactRegistry(
    val context: AggregateContext,
    private val permissionManager: PermissionManager
) {
    private val artifacts: MutableMap<ArtifactId, Artifact> = context.getMap("registry/v1/artifacts")
    private val artifactsByVersion: MutableMap<VersionId, MutableList<Artifact>> =
        context.getMap("registry/v1/artifacts-by-version")

    suspend fun getAll(versionIds: Collection<VersionId>) = artifactsByVersion
        .filterKeys { versionIds.contains(it) }
        .values
        .flatten()
        .artifactsFilteredForUser()
        .asSequence()

    /**
     * Get artifact based on id
     */
    suspend fun getById(artifactId: ArtifactId): Artifact {
        val single = artifacts[artifactId] ?: throw NotFoundException("Unknown artifact id: $artifactId")
        if (!permissionManager.hasPermission(
                single.version.schema.namespace.groupId,
                GroupPermission.READ
            )
        ) throw ForbiddenException("Forbidden to read artifact: ${single.filename}")
        return single
    }

    suspend fun getByName(
        versionId: VersionId,
        filename: String
    ): Artifact {
        return artifactsByVersion[versionId].orEmpty()
            .artifactsFilteredForUser()
            .singleOrNull {
                it.filename == filename
            }
            ?: throw NotFoundException("Unknown artifact: $filename in version $versionId")
    }

    fun exists(versionId: VersionId, filename: String) = artifactsByVersion[versionId]?.any {
        it.filename == filename
    } ?: false

    fun add(artifact: Artifact) {
        artifacts[artifact.id] = artifact
        artifactsByVersion.getOrPut(artifact.version.id) { mutableListOf() }.add(artifact)
    }

    suspend fun removeById(artifactId: ArtifactId) {
        val artifact = getById(artifactId)
        artifacts.remove(artifactId)
        artifactsByVersion[artifact.version.id]!!.remove(artifact)
    }

    private suspend fun Collection<Artifact>.artifactsFilteredForUser() =
        filter { permissionManager.hasPermission(it.version.schema.namespace.groupId, GroupPermission.READ) }
}