package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.State
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

data class RegistryState(
    private val context: AggregateContext,
    private val permissionManager: PermissionManager,
    internal val namespaces: MutableMap<NamespaceId, Namespace> = context.getMap("registry/v1/namespaces"),
    internal val schemas: MutableMap<SchemaId, Schema> = context.getMap("registry/v1/schemas"),
    internal val versions: MutableMap<VersionId, Version> = context.getMap("registry/v1/versions"),
    internal val artifacts: MutableMap<ArtifactId, Artifact> = context.getMap("registry/v1/artifacts")
) : State {
    /**
     * Get all available namespaces
     */
    suspend fun getNamespaces(): Collection<Namespace> = namespaces.values.filteredForUser()

    /**
     * Get namespace based on id
     */
    suspend fun getNamespace(namespaceId: NamespaceId): Namespace {
        val single = namespaces[namespaceId] ?: throw NotFoundException("Unknown namespace id: $namespaceId")
        if (!permissionManager.hasPermission(single, GroupPermission.READ)) throw ForbiddenException()
        return single
    }

    suspend fun findNamespace(namespace: String): Namespace {
        val filtered = namespaces.values.filteredForUser()
        return filtered.firstOrNull { it.name == namespace }
            ?: throw NotFoundException("Unknown namespace: $namespace")
    }

    fun findUnauthorizedNamespace(namespace: String) = namespaces.values
        .firstOrNull { it.name == namespace }
        ?: throw NotFoundException("Unknown namespace: $namespace")

    /**
     * Get all available schemas
     */
    suspend fun getSchemas(): Collection<Schema> = schemas.values.filteredForUser()

    /**
     * Get all schemas for the specified namespaces
     */
    suspend fun getSchemas(namespaceIds: Collection<NamespaceId>): Collection<Schema> = schemas.values
        .filter {
            namespaceIds.any { id ->
                it.namespace.id == id
            }
        }.filteredForUser()

    /**
     * Get schema based on id
     */
    suspend fun getSchema(schemaId: SchemaId): Schema {
        val single = schemas[schemaId] ?: throw NotFoundException("Unknown schema id: $schemaId")
        if (!permissionManager.hasPermission(single, GroupPermission.READ)) throw ForbiddenException()
        return single
    }

    suspend fun findSchema(namespaceId: NamespaceId, schema: String) =
        schemas.values
            .filteredForUser().singleOrNull {
                it.namespace.id == namespaceId && it.name == schema
            }
            ?: throw NotFoundException("Unknown schema id: $schema in namespace with id: $namespaceId")

    suspend fun getVersions(schemaId: SchemaId) = versions.values
        .filteredForUser()
        .filter {
            it.schema.id == schemaId
        }

    /**
     * Get version based on id
     */
    suspend fun getVersion(versionId: VersionId): Version {
        val single = versions[versionId] ?: throw NotFoundException("Unknown version id: $versionId")
        if (!permissionManager.hasPermission(single, GroupPermission.READ)) throw ForbiddenException()
        return single
    }

    /**
     * Get the current major versions
     */
    suspend fun getCurrentMajorVersions(schemaId: SchemaId): Collection<Version> {
        return versions.values
            .filteredForUser()
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
    }

    /**
     * Is this a current version (i.e. the latest stable version of a major version)?
     */
    suspend fun isCurrent(version: Version) =
        getCurrentMajorVersions(version.schema.id).contains(version)

    suspend fun findVersion(namespaceId: NamespaceId, schemaId: SchemaId, version: String) = versions.values
        .filteredForUser().singleOrNull {
            it.schema.namespace.id == namespaceId && it.schema.id == schemaId && it.semVer.value == version
        }
        ?: throw NotFoundException("Unknown version: $version in schema with id: $schemaId and namespace with id: $namespaceId")

    suspend fun getArtifacts() = artifacts.values.filteredForUser()

    suspend fun getArtifacts(versionIds: Collection<VersionId>) = artifacts.values
        .filteredForUser()
        .filter {
            versionIds.any { id ->
                it.version.id == id
            }
        }

    /**
     * Get artifact based on id
     */
    suspend fun getArtifact(artifactId: ArtifactId): Artifact {
        val single = artifacts[artifactId] ?: throw NotFoundException("Unknown artifact id: $artifactId")
        if (!permissionManager.hasPermission(single, GroupPermission.READ)) throw ForbiddenException()
        return single
    }

    suspend fun findArtifact(namespaceId: NamespaceId, schemaId: SchemaId, versionId: VersionId, filename: String) =
        artifacts.values
            .filteredForUser()
            .filter {
                it.version.schema.namespace.id == namespaceId && it.version.schema.id == schemaId && it.version.id == versionId && it.filename == filename
            }
            .singleOrNull()
            ?: throw NotFoundException("Unknown artifact: $filename in version with id: $versionId in schema with id: $schemaId and namespace with id: $namespaceId")

    private suspend fun <T> Collection<T>.filteredForUser(): Collection<T> {
        return this.filter { permissionManager.hasPermission(it, GroupPermission.READ) }
    }
}
