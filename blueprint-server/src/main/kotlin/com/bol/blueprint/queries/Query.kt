package com.bol.blueprint.queries

import com.bol.blueprint.domain.*
import com.bol.blueprint.queries.SinkHandlerBuilder.Companion.sinkHandler

class Query : Sink, Resettable {
    private val namespaces = mutableMapOf<NamespaceId, Namespace>()

    private val schemas = mutableMapOf<SchemaId, Schema>()
    private val schemaNamespaces = mutableMapOf<SchemaId, NamespaceId>()

    private val versions = mutableMapOf<VersionId, Version>()
    private val versionSchemas = mutableMapOf<VersionId, SchemaId>()

    private val artifacts = mutableMapOf<ArtifactId, Artifact>()
    private val artifactVersions = mutableMapOf<ArtifactId, VersionId>()

    private val handler = sinkHandler {
        handle<NamespaceCreatedEvent> {
            namespaces[it.id] = Namespace(it.id, it.name, it.group)
        }
        handle<NamespaceDeletedEvent> {
            namespaces.remove(it.id)
        }
        handle<SchemaCreatedEvent> {
            val schema = Schema(it.id, it.name, it.schemaType)
            schemas[it.id] = schema
            schemaNamespaces[it.id] = it.namespaceId
        }
        handle<SchemaDeletedEvent> {
            schemas.remove(it.id)
            schemaNamespaces.remove(it.id)
        }
        handle<VersionCreatedEvent> {
            val version = Version(it.id, it.version)
            versions[it.id] = version
            versionSchemas[it.id] = it.schemaId
        }
        handle<VersionDeletedEvent> {
            versions.remove(it.id)
            versionSchemas.remove(it.id)
        }
        handle<ArtifactCreatedEvent> {
            val artifact = Artifact(it.id, it.filename, it.mediaType)
            artifacts[it.id] = artifact
            artifactVersions[it.id] = it.versionId
        }
        handle<ArtifactDeletedEvent> {
            artifacts.remove(it.id)
            artifactVersions.remove(it.id)
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
     * Get namespace based on id
     */
    fun getNamespace(namespaceId: NamespaceId): Namespace? = namespaces[namespaceId]

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
    fun getSchemas(namespaceIds: Collection<NamespaceId>): Collection<Schema> = schemas.filter {
        namespaceIds.any { id ->
            schemaNamespaces[it.key] == id
        }
    }.values

    /**
     * Get schema based on id
     */
    fun getSchema(schemaId: SchemaId): Schema? = schemas[schemaId]

    fun getSchemaNamespace(schema: Schema): Namespace? = schemaNamespaces[schema.id]?.let { namespaces[it] }

    fun getSchemaNamespaceOrThrow(schema: Schema) = getSchemaNamespace(schema)
            ?: throw RuntimeException("Could not find the namespace belonging to schema: $schema")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Versions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getVersions(): Collection<Version> = versions.values

    fun getVersions(schemaIds: Collection<SchemaId>): Collection<Version> = versions.filter {
        schemaIds.any { id ->
            versionSchemas[it.key] == id
        }
    }.values

    /**
     * Get version based on id
     */
    fun getVersion(versionId: VersionId): Version? = versions[versionId]

    fun getVersionSchema(version: Version): Schema? = versionSchemas[version.id]?.let { schemas[it] }

    fun getVersionSchemaOrThrow(version: Version) = getVersionSchema(version)
            ?: throw RuntimeException("Could not find the schema belonging to version: $version")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Artifacts
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getArtifacts(): Collection<Artifact> = artifacts.values

    fun getArtifacts(versionIds: Collection<VersionId>) = artifacts.filter {
        versionIds.any { id ->
            artifactVersions[it.key] == id
        }
    }.values

    /**
     * Get artifact based on id
     */
    fun getArtifact(artifactId: ArtifactId): Artifact? = artifacts[artifactId]

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