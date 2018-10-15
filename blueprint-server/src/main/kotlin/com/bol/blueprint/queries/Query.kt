package com.bol.blueprint.queries

import com.bol.blueprint.domain.*
import com.bol.blueprint.queries.SinkHandlerBuilder.Companion.sinkHandler

class Query : Sink, Resettable {
    private val namespaces = mutableMapOf<NamespaceKey, Namespace>()
    private val schemas = mutableMapOf<SchemaKey, Schema>()
    private val versions = mutableMapOf<VersionKey, Version>()
    private val artifacts = mutableMapOf<ArtifactKey, Artifact>()

    private val handler = sinkHandler {
        handle<NamespaceCreatedEvent> {
            namespaces[it.key] = Namespace(it.key.namespace, it.groupKey)
        }
        handle<NamespaceDeletedEvent> {
            namespaces.remove(it.key)
        }
        handle<SchemaCreatedEvent> {
            val schema = Schema(it.key.schema, it.schemaType)
            schemas[it.key] = schema
        }
        handle<SchemaDeletedEvent> {
            schemas.remove(it.key)
        }
        handle<VersionCreatedEvent> {
            val version = Version(it.key.version)
            versions[it.key] = version
        }
        handle<VersionDeletedEvent> {
            versions.remove(it.key)
        }
        handle<ArtifactCreatedEvent> {
            val artifact = Artifact(it.key.filename, it.mediaType, it.path)
            artifacts[it.key] = artifact
        }
        handle<ArtifactDeletedEvent> {
            artifacts.remove(it.key)
        }
    }

    override fun reset() {
        namespaces.clear()
        schemas.clear()
        versions.clear()
        artifacts.clear()
    }

    override fun <T : Any> getHandler() = handler

    fun getNamespaces() = namespaces.values.asSequence()

    fun getNamespace(key: NamespaceKey) = namespaces[key]

    fun getSchemas(key: NamespaceKey) = schemas.entries.asSequence().filter { it.key.namespace == key.namespace }.map { it.value }

    fun getSchema(key: SchemaKey) = schemas[key]

    fun getVersions(key: SchemaKey) = versions.entries.asSequence().filter { it.key.namespace == key.namespace && it.key.schema == key.schema }.map { it.value }

    fun getVersion(key: VersionKey) = versions[key]

    fun getArtifacts(key: VersionKey) = artifacts.entries.asSequence().filter { it.key.namespace == key.namespace && it.key.schema == key.schema && it.key.version == key.version }.map { it.value }

    fun getArtifact(key: ArtifactKey) = artifacts[key]

    /*fun getVersionRange(key: SchemaKey, rangeStart: String?, rangeStop: String?) = {
        VersionRangeQuery(getFilteredVersions(key), Semver.SemverType.IVY).getVersionRange(rangeStart, rangeStop)
    }

    private fun getFilteredVersions(key: SchemaKey): Collection<Version> =
            versions.entries.filter { it.key.namespace == key.namespace && it.key.schema == key.schema }.map { it.value }*/
}