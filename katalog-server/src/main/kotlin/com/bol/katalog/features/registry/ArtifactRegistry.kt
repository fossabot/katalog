package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.hazelcast.HazelcastAggregateContext
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

class ArtifactRegistry(
    private val registry: RegistryAggregate,
    private val context: HazelcastAggregateContext,
    private val permissionManager: PermissionManager
) {
    suspend fun getByVersion(versionId: VersionId) = getArtifactsByVersion()[versionId].orEmpty()
        .artifactsFilteredForUser()
        .asSequence()

    /**
     * Get artifact based on id
     */
    suspend fun getById(artifactId: ArtifactId): Artifact {
        val single = getArtifacts()[artifactId] ?: throw NotFoundException("Unknown artifact id: $artifactId")
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

    suspend fun exists(versionId: VersionId, filename: String) = getArtifactsByVersion()[versionId]?.any {
        it.filename == filename
    } ?: false

    suspend fun add(artifact: Artifact) {
        getMutableArtifacts()[artifact.id] = artifact
        getMutableArtifactsByVersion().put(artifact.versionId, artifact)
    }

    suspend fun removeById(artifactId: ArtifactId) {
        val artifact = getById(artifactId)
        getMutableArtifacts().remove(artifactId)
        getMutableArtifactsByVersion().remove(artifact.versionId, artifact)
    }

    private suspend fun Collection<Artifact>.artifactsFilteredForUser() =
        filter { permissionManager.hasPermissionBy(it, GroupPermission.READ) }

    private suspend fun getArtifacts() = context.map<ArtifactId, Artifact>("registry/v1/artifacts")
    private suspend fun getMutableArtifacts() = context.txMap<ArtifactId, Artifact>("registry/v1/artifacts")

    private suspend fun getArtifactsByVersion() =
        context.multiMap<VersionId, Artifact>("registry/v1/artifacts-by-version")

    private suspend fun getMutableArtifactsByVersion() =
        context.txMultiMap<VersionId, Artifact>("registry/v1/artifacts-by-version")

    suspend fun reset() {
        getMutableArtifacts().destroy()
        getMutableArtifactsByVersion().destroy()
    }
}