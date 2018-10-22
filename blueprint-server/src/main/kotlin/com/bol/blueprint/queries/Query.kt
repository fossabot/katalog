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
    fun getNamespace(id: NamespaceKey): Namespace? = namespaces[id]

    /**
     * Get all available schemas
     */
    fun getSchemas() = schemas

    /**
     * Get all schemas for the specified namespaces
     */
    fun getSchemas(namespaces: Collection<NamespaceKey>): Map<SchemaKey, Schema> = schemas.filter {
        namespaces.any { namespaceKey ->
            schemaNamespaces[it.key] == namespaceKey
        }
    }

    /**
     * Get schema based on key
     */
    fun getSchema(id: SchemaKey): Schema? = schemas[id]

    fun getSchemaNamespace(schema: SchemaKey): NamespaceKey? = schemaNamespaces[schema]

    fun getVersions(schemas: Collection<SchemaKey>): Map<VersionKey, Version> = versions.filter {
        schemas.any { schemaKey ->
            versionSchemas[it.key] == schemaKey
        }
    }

    fun getVersionSchema(version: VersionKey): SchemaKey? = versionSchemas[version]

    fun getArtifacts(versions: Collection<VersionKey>) = artifacts.filter {
        versions.any { versionKey ->
            artifactVersions[it.key] == versionKey
        }
    }

    fun getArtifactVersion(artifact: ArtifactKey): VersionKey? = artifactVersions[artifact]

    /*fun getVersionRange(key: SchemaKey, rangeStart: String?, rangeStop: String?) = {
        VersionRangeQuery(getFilteredVersions(key), Semver.SemverType.IVY).getVersionRange(rangeStart, rangeStop)
    }

    private fun getFilteredVersions(key: SchemaKey): Collection<Version> =
            versions.entries.filter { it.key.namespace == key.namespace && it.key.schema == key.schema }.map { it.value }*/
}