package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

class ArtifactRegistry(
    context: AggregateContext,
    private val permissionManager: PermissionManager
) {
    private val artifacts = context.map<ArtifactId, Artifact>("registry/v1/artifacts")
    private val artifactsByVersion =
        context.multiMap<VersionId, Artifact>("registry/v1/artifacts-by-version")

    suspend fun getByVersion(versionId: VersionId) = artifactsByVersion.read { this[versionId].orEmpty() }
        .artifactsFilteredForUser()
        .asSequence()

    /**
     * Get artifact based on id
     */
    suspend fun getById(artifactId: ArtifactId): Artifact {
        val single =
            artifacts.read { this[artifactId] } ?: throw NotFoundException("Unknown artifact id: $artifactId")
        if (!permissionManager.hasPermissionBy(
                single,
                GroupPermission.READ
            )
        ) throw ForbiddenException("Forbidden to read artifact: ${single.filename}")
        return single
    }

    suspend fun getByName(
        versionId: VersionId,
        filename: String
    ): Artifact {
        return getByVersion(versionId).singleOrNull { it.filename == filename }
            ?: throw NotFoundException("Unknown artifact: $filename in version $versionId")
    }

    suspend fun exists(versionId: VersionId, filename: String) = artifactsByVersion.read {
        this[versionId]?.any {
            it.filename == filename
        }
    } ?: false

    suspend fun add(artifact: Artifact) {
        artifacts.write { this[artifact.id] = artifact }
        artifactsByVersion.write { put(artifact.versionId, artifact) }
    }

    suspend fun removeById(artifactId: ArtifactId) {
        val artifact = getById(artifactId)
        artifacts.write { remove(artifactId) }
        artifactsByVersion.write { remove(artifact.versionId, artifact) }
    }

    private suspend fun Collection<Artifact>.artifactsFilteredForUser() =
        filter { permissionManager.hasPermissionBy(it, GroupPermission.READ) }

    fun reset() {
        artifacts.reset()
        artifactsByVersion.reset()
    }
}