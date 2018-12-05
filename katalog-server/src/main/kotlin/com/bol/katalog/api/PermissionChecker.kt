package com.bol.katalog.api

import com.bol.katalog.domain.*
import com.bol.katalog.domain.aggregates.ArtifactAggregate
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.domain.aggregates.SchemaAggregate
import com.bol.katalog.domain.aggregates.VersionAggregate
import com.bol.katalog.security.CoroutineUserContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class PermissionChecker(
    val namespaces: NamespaceAggregate,
    val schemas: SchemaAggregate,
    val versions: VersionAggregate,
    val artifacts: ArtifactAggregate
) {
    suspend fun require(groupName: String, permission: GroupPermission) = require(Group(groupName), permission)

    suspend fun require(group: Group, permission: GroupPermission) {
        val isAllowed = CoroutineUserContext.get()
            ?.hasGroupPermission(group, permission)
            ?: false

        if (!isAllowed) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    suspend fun requireNamespace(namespaceId: NamespaceId, permission: GroupPermission) {
        require(namespaces.getNamespace(namespaceId).group, permission)
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
