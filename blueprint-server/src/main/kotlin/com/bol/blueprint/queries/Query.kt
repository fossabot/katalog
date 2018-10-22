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
            namespaces[it.key] = Namespace(it.key, it.name, it.group)
        }
        handle<NamespaceDeletedEvent> {
            namespaces.remove(it.key)
        }
        handle<SchemaCreatedEvent> {
            val schema = Schema(it.key, it.name, it.schemaType)
            schemas[it.key] = schema
            schemaNamespaces[it.key] = it.namespace
        }
        handle<SchemaDeletedEvent> {
            schemas.remove(it.key)
            schemaNamespaces.remove(it.key)
        }
        handle<VersionCreatedEvent> {
            val version = Version(it.key, it.version)
            versions[it.key] = version
            versionSchemas[it.key] = it.schema
        }
        handle<VersionDeletedEvent> {
            versions.remove(it.key)
            versionSchemas.remove(it.key)
        }
        handle<ArtifactCreatedEvent> {
            val artifact = Artifact(it.key, it.filename, it.mediaType)
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Namespaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get all available namespaces
     */
    fun getNamespaces(): Collection<Namespace> = namespaces.values

    /**
     * Get namespace based on key
     */
    fun getNamespace(namespaceKey: NamespaceKey): Namespace? = namespaces[namespaceKey]

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Schemas
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get all available schemas
     */
    fun getSchemas(): Collection<Schema> = schemas.values

    /**
     * Get all schemas for the specified namespaces
     */
    fun getSchemas(namespaceKeys: Collection<NamespaceKey>): Collection<Schema> = schemas.filter {
        namespaceKeys.any { namespaceKey ->
            schemaNamespaces[it.key] == namespaceKey
        }
    }.values

    /**
     * Get schema based on key
     */
    fun getSchema(schemaKey: SchemaKey): Schema? = schemas[schemaKey]

    fun getSchemaNamespace(schema: Schema): Namespace? = schemaNamespaces[schema.id]?.let { namespaces[it] }

    fun getSchemaNamespaceOrThrow(schema: Schema) = getSchemaNamespace(schema)
            ?: throw RuntimeException("Could not find the namespace belonging to schema: $schema")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Versions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getVersions(): Collection<Version> = versions.values

    fun getVersions(schemaKeys: Collection<SchemaKey>): Collection<Version> = versions.filter {
        schemaKeys.any { schemaKey ->
            versionSchemas[it.key] == schemaKey
        }
    }.values

    /**
     * Get version based on key
     */
    fun getVersion(versionKey: VersionKey): Version? = versions[versionKey]

    fun getVersionSchema(version: Version): Schema? = versionSchemas[version.id]?.let { schemas[it] }

    fun getVersionSchemaOrThrow(version: Version) = getVersionSchema(version)
            ?: throw RuntimeException("Could not find the schema belonging to version: $version")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Artifacts
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getArtifacts(): Collection<Artifact> = artifacts.values

    fun getArtifacts(versionKeys: Collection<VersionKey>) = artifacts.filter {
        versionKeys.any { versionKey ->
            artifactVersions[it.key] == versionKey
        }
    }.values

    /**
     * Get artifact based on key
     */
    fun getArtifact(artifactKey: ArtifactKey): Artifact? = artifacts[artifactKey]

    fun getArtifactVersion(artifact: Artifact): Version? = artifactVersions[artifact.id]?.let { versions[it] }

    fun getArtifactVersionOrThrow(artifact: Artifact) = getArtifactVersion(artifact)
            ?: throw RuntimeException("Could not find the version belonging to artifact: $artifact")

    fun findArtifact(namespace: String, schema: String, version: String, filename: String) =
            namespaces.values.singleOrNull { it.name == namespace }.let { foundNamespace ->
                schemas.values.singleOrNull {
                    getSchemaNamespace(it) == foundNamespace && it.name == schema
                }.let { foundSchema ->
                    versions.values.singleOrNull {
                        getVersionSchema(it) == foundSchema && it.version == version
                    }.let { foundVersion ->
                        artifacts.values.singleOrNull {
                            getArtifactVersion(it) == foundVersion && it.filename == filename
                        }
                    }
                }
            }
}