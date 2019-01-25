package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.State
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.users.GroupPermission

data class RegistryState(
    private val context: AggregateContext,
    private val security: SecurityAggregate,
    internal val namespaces: MutableMap<NamespaceId, Namespace> = context.getMap("registry/v1/namespaces"),
    internal val schemas: MutableMap<SchemaId, SchemaEntry> = context.getMap("registry/v1/schemas"),
    internal val versions: MutableMap<VersionId, VersionEntry> = context.getMap("registry/v1/versions"),
    internal val artifacts: MutableMap<ArtifactId, ArtifactEntry> = context.getMap("registry/v1/artifacts")
) : State {
    data class SchemaEntry(val namespaceId: NamespaceId, val schemaId: SchemaId, val schema: Schema)

    data class VersionEntry(
        val namespaceId: NamespaceId,
        val schemaId: SchemaId,
        val versionId: VersionId,
        val version: Version
    )

    data class ArtifactEntry(
        val namespaceId: NamespaceId,
        val schemaId: SchemaId,
        val versionId: VersionId,
        val artifact: Artifact
    )


    /**
     * Get all available namespaces
     */
    suspend fun getNamespaces(): Collection<Namespace> = filteredForUser(namespaces.values)

    /**
     * Get namespace based on id
     */
    suspend fun getNamespace(namespaceId: NamespaceId): Namespace {
        val filtered = filteredForUser(listOfNotNull(namespaces[namespaceId]))
        return filtered.singleOrNull()
            ?: throw NotFoundException("Unknown namespace id: $namespaceId")
    }

    suspend fun findNamespace(namespace: String): Namespace {
        val filtered = filteredForUser(namespaces.values)
        return filtered.firstOrNull { it.name == namespace }
            ?: throw NotFoundException("Unknown namespace: $namespace")
    }

    fun findUnauthorizedNamespace(namespace: String) = namespaces.values
        .firstOrNull { it.name == namespace }
        ?: throw NotFoundException("Unknown namespace: $namespace")

    // Filter the namespaces based on user, or remove them all if the user is null
    private suspend fun filteredForUser(namespaces: Collection<Namespace>): Collection<Namespace> {
        return CoroutineUserContext.get()?.let { user ->
            return namespaces.filter {
                security.read { hasPermission(user, it.groupId, GroupPermission.READ) }
            }
        } ?: emptyList()
    }

    /**
     * Get all available schemas
     */
    suspend fun getSchemas(): Collection<Schema> = schemas.values.map { it.schema }

    /**
     * Get all schemas for the specified namespaces
     */
    suspend fun getSchemas(namespaceIds: Collection<NamespaceId>): Collection<Schema> = schemas.filter {
        namespaceIds.any { id ->
            it.value.namespaceId == id
        }
    }.map { it.value.schema }

    /**
     * Get schema based on id
     */
    suspend fun getSchema(schemaId: SchemaId) =
        schemas[schemaId]?.schema ?: throw NotFoundException("Unknown schema id: $schemaId")

    suspend fun getSchemaNamespaceId(schemaId: SchemaId) = schemas[schemaId]?.namespaceId
        ?: throw NotFoundException("Unknown schema id: $schemaId")

    suspend fun findSchema(namespaceId: NamespaceId, schema: String) =
        schemas.values
            .filter {
                it.namespaceId == namespaceId && it.schema.name == schema
            }
            .map { it.schema }
            .singleOrNull()
            ?: throw NotFoundException("Unknown schema id: $schema in namespace with id: $namespaceId")

    suspend fun getVersions(schemaId: SchemaId) = versions.filter {
        it.value.schemaId == schemaId
    }.map { it.value.version }

    /**
     * Get version based on id
     */
    suspend fun getVersion(versionId: VersionId) =
        versions[versionId]?.version ?: throw NotFoundException("Unknown version id: $versionId")

    suspend fun getVersionSchemaId(versionId: VersionId) = versions[versionId]?.schemaId
        ?: throw NotFoundException("Unknown version id: $versionId")

    /**
     * Get the current major versions
     */
    suspend fun getCurrentMajorVersions(versions: Collection<Version>): Collection<Version> {
        return versions
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
    suspend fun isCurrent(schemaId: SchemaId, version: Version) =
        getCurrentMajorVersions(getVersions(schemaId)).contains(version)

    suspend fun findVersion(namespaceId: NamespaceId, schemaId: SchemaId, version: String) = versions.values
        .filter {
            it.namespaceId == namespaceId && it.schemaId == schemaId && it.version.semVer.value == version
        }
        .map { it.version }
        .singleOrNull()
        ?: throw NotFoundException("Unknown version: $version in schema with id: $schemaId and namespace with id: $namespaceId")

    suspend fun getArtifacts() = artifacts.values.map { it.artifact }

    suspend fun getArtifacts(versionIds: Collection<VersionId>) = artifacts.filter {
        versionIds.any { id ->
            it.value.versionId == id
        }
    }.map { it.value.artifact }

    /**
     * Get artifact based on id
     */
    suspend fun getArtifact(artifactId: ArtifactId) =
        artifacts[artifactId]?.artifact ?: throw NotFoundException("Unknown artifact id: $artifactId")

    suspend fun getArtifactVersionId(artifactId: ArtifactId) = artifacts[artifactId]?.versionId
        ?: throw NotFoundException("Unknown artifact id: $artifactId")

    suspend fun findArtifact(namespaceId: NamespaceId, schemaId: SchemaId, versionId: VersionId, filename: String) =
        artifacts.values
            .filter {
                it.namespaceId == namespaceId && it.schemaId == schemaId && it.versionId == versionId && it.artifact.filename == filename
            }
            .map { it.artifact }
            .singleOrNull()
            ?: throw NotFoundException("Unknown artifact: $filename in version with id: $versionId in schema with id: $schemaId and namespace with id: $namespaceId")

    suspend fun getOwner(artifactId: ArtifactId) =
        artifacts[artifactId]?.let { Triple(it.namespaceId, it.schemaId, it.versionId) }
            ?: throw NotFoundException("Unknown artifact with id: $artifactId")
}