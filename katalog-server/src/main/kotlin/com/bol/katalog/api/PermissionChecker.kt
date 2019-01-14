package com.bol.katalog.api

import com.bol.katalog.features.registry.ArtifactId
import com.bol.katalog.features.registry.NamespaceId
import com.bol.katalog.features.registry.SchemaId
import com.bol.katalog.features.registry.VersionId
import com.bol.katalog.features.registry.aggregates.ArtifactAggregate
import com.bol.katalog.features.registry.aggregates.NamespaceAggregate
import com.bol.katalog.features.registry.aggregates.SchemaAggregate
import com.bol.katalog.features.registry.aggregates.VersionAggregate
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.users.GroupPermission
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class PermissionChecker(
    val namespaces: NamespaceAggregate,
    val schemas: SchemaAggregate,
    val versions: VersionAggregate,
    val artifacts: ArtifactAggregate,
    val security: SecurityAggregate
) {
    suspend fun require(groupId: GroupId, permission: GroupPermission) {
        val isAllowed = CoroutineUserContext.get()?.let { user ->
            security.hasPermission(user, groupId, permission)
        } ?: false

        if (!isAllowed) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    suspend fun requireNamespace(namespaceId: NamespaceId, permission: GroupPermission) {
        require(namespaces.getNamespace(namespaceId).groupId, permission)
    }

    suspend fun requireSchema(schemaId: SchemaId, permission: GroupPermission) {
        requireNamespace(schemas.getSchemaNamespaceId(schemaId), permission)
    }

    suspend fun requireVersion(versionId: VersionId, permission: GroupPermission) {
        requireSchema(versions.getVersionSchemaId(versionId), permission)
    }

    suspend fun requireArtifact(artifactId: ArtifactId, permission: GroupPermission) {
        requireVersion(artifacts.getArtifactVersionId(artifactId), permission)
    }
}
