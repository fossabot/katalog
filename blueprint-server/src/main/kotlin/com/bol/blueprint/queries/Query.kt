package com.bol.blueprint.queries

import com.bol.blueprint.domain.*
import com.bol.blueprint.queries.SinkHandlerBuilder.Companion.sinkHandler

class Query : Sink, Resettable {
    private val namespaces = mutableMapOf<NamespaceKey, Namespace>()

    private val schemas = mutableMapOf<SchemaKey, Schema>()
    private val schemaNamespaces = mutableMapOf<SchemaKey, NamespaceKey>()

    private val versions = mutableMapOf<VersionKey, Version>()
    private val versionSchemas = mutableMapOf<VersionKey, SchemaKey>()

    private val artifacts = mutableMapOf<ArtifactKey, Artifact>()
    private val artifactVersions = mutableMapOf<ArtifactKey, VersionKey>()

    private val handler = sinkHandler {
        handle<NamespaceCreatedEvent> {
            namespaces[it.key] = Namespace(it.name, it.group)
        }
        handle<NamespaceDeletedEvent> {
            namespaces.remove(it.key)
        }
        handle<SchemaCreatedEvent> {
            val schema = Schema(it.name, it.schemaType)
            schemas[it.key] = schema
            schemaNamespaces[it.key] = it.namespace
        }
        handle<SchemaDeletedEvent> {
            schemas.remove(it.key)
            schemaNamespaces.remove(it.key)
        }
        handle<VersionCreatedEvent> {
            val version = Version(it.version)
            versions[it.key] = version
            versionSchemas[it.key] = it.schema
        }
        handle<VersionDeletedEvent> {
            versions.remove(it.key)
            versionSchemas.remove(it.key)
        }
        handle<ArtifactCreatedEvent> {
            val artifact = Artifact(it.filename, it.mediaType, it.path)
            artifacts[it.key] = artifact
            artifactVersions[it.key] = it.version
        }
        handle<ArtifactDeletedEvent> {
            artifacts.remove(it.key)
            artifactVersions.remove(it.key)
        }
    }

    override fun reset() {
        namespaces.clear()
        schemas.clear()
        versions.clear()
        artifacts.clear()
    }

    override fun <T : Any> getHandler() = handler

    /**
     * Get all available namespaces
     */
    fun getNamespaces(): Map<NamespaceKey, Namespace> = namespaces

    /**
     * Get namespace based on key
     */
    fun getNamespace(namespaceKey: NamespaceKey): Namespace? = namespaces[namespaceKey]

    /**
     * Get namespacekey based on name
     */
    fun getNamespaceKey(name: String): NamespaceKey? = namespaces.filter {
        it.value.name == name
    }.keys.singleOrNull()

    /**
     * Get all available schemas
     */
    fun getSchemas() = schemas

    /**
     * Get all schemas for the specified namespaces
     */
    fun getSchemas(namespaceKeys: Collection<NamespaceKey>): Map<SchemaKey, Schema> = schemas.filter {
        namespaceKeys.any { namespaceKey ->
            schemaNamespaces[it.key] == namespaceKey
        }
    }

    /**
     * Get schema based on key
     */
    fun getSchema(schemaKey: SchemaKey): Schema? = schemas[schemaKey]

    fun getSchemaNamespace(schemaKey: SchemaKey): NamespaceKey? = schemaNamespaces[schemaKey]

    fun getSchemaNamespaceOrThrow(schemaKey: SchemaKey) = getSchemaNamespace(schemaKey)
            ?: throw RuntimeException("Could not find the namespace belonging to schema: $schemaKey")

    /**
     * Get schemakey based on name
     */
    fun getSchemaKey(namespaceKey: NamespaceKey, name: String): SchemaKey? = getSchemas(listOf(namespaceKey)).filter {
        it.value.name == name
    }.keys.singleOrNull()

    fun getVersions() = versions

    fun getVersions(schemaKeys: Collection<SchemaKey>): Map<VersionKey, Version> = versions.filter {
        schemaKeys.any { schemaKey ->
            versionSchemas[it.key] == schemaKey
        }
    }

    /**
     * Get version based on key
     */
    fun getVersion(versionKey: VersionKey): Version? = versions[versionKey]

    fun getVersionSchema(versionKey: VersionKey): SchemaKey? = versionSchemas[versionKey]

    fun getVersionSchemaOrThrow(versionKey: VersionKey) = getVersionSchema(versionKey)
            ?: throw RuntimeException("Could not find the schema belonging to version: $versionKey")

    /**
     * Get versionkey based on name
     */
    fun getVersionKey(schemaKey: SchemaKey, version: String): VersionKey? = getVersions(listOf(schemaKey)).filter {
        it.value.version == version
    }.keys.singleOrNull()

    /**
     * Get artifact based on key
     */
    fun getArtifact(artifactKey: ArtifactKey): Artifact? = artifacts[artifactKey]

    fun getArtifacts() = artifacts

    fun getArtifacts(versionKeys: Collection<VersionKey>) = artifacts.filter {
        versionKeys.any { versionKey ->
            artifactVersions[it.key] == versionKey
        }
    }

    fun getArtifactVersion(artifactKey: ArtifactKey): VersionKey? = artifactVersions[artifactKey]

    fun getArtifactVersionOrThrow(artifactKey: ArtifactKey) = getArtifactVersion(artifactKey)
            ?: throw RuntimeException("Could not find the version belonging to artifact: $artifactKey")

    /**
     * Get artifactkey based on name
     */
    fun getArtifactKey(versionKey: VersionKey, filename: String): ArtifactKey? = getArtifacts(listOf(versionKey)).filter {
        it.value.filename == filename
    }.keys.singleOrNull()

    /*fun getVersionRange(key: SchemaKey, rangeStart: String?, rangeStop: String?) = {
        VersionRangeQuery(getFilteredVersions(key), Semver.SemverType.IVY).getVersionRange(rangeStart, rangeStop)
    }

    private fun getFilteredVersions(key: SchemaKey): Collection<Version> =
            versions.entries.filter { it.key.namespace == key.namespace && it.key.schema == key.schema }.map { it.value }*/
}