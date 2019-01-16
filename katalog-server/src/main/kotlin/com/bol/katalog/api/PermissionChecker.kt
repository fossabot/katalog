package com.bol.katalog.api

import com.bol.katalog.features.registry.*
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.users.GroupPermission
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class PermissionChecker(
    val registry: RegistryAggregate,
    val security: SecurityAggregate
) {
    suspend fun require(groupId: GroupId, permission: GroupPermission) {
        val isAllowed = CoroutineUserContext.get()?.let { user ->
            security.read { hasPermission(user, groupId, permission) }
        } ?: false

        if (!isAllowed) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    suspend fun requireNamespace(namespaceId: NamespaceId, permission: GroupPermission) {
        require(registry.read { getNamespace(namespaceId).groupId }, permission)
    }

    suspend fun requireSchema(schemaId: SchemaId, permission: GroupPermission) {
        requireNamespace(registry.read { getSchemaNamespaceId(schemaId) }, permission)
    }

    suspend fun requireVersion(versionId: VersionId, permission: GroupPermission) {
        requireSchema(registry.read { getVersionSchemaId(versionId) }, permission)
    }

    suspend fun requireArtifact(artifactId: ArtifactId, permission: GroupPermission) {
        requireVersion(registry.read { getArtifactVersionId(artifactId) }, permission)
    }
}
