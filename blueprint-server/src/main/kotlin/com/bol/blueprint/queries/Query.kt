package com.bol.blueprint.queries

import com.bol.blueprint.domain.*
import mu.KotlinLogging

class Query : Sink<Event>, Resettable {
    private val log = KotlinLogging.logger {}

    private val namespaces = mutableMapOf<NamespaceKey, Namespace>()
    private val schemas = mutableMapOf<SchemaKey, Schema>()
    private val versions = mutableMapOf<VersionKey, Version>()
    private val artifacts = mutableMapOf<ArtifactKey, Artifact>()

    override fun reset() {
        namespaces.clear()
        schemas.clear()
        versions.clear()
        artifacts.clear()
    }

    override fun getHandler(): suspend (Event) -> Unit {
        return {
            log.debug("Received: $it")
            when (it) {
                is NamespaceCreatedEvent -> {
                    namespaces[it.key] = Namespace(it.key.namespace)
                }
                is NamespaceDeletedEvent -> {
                    namespaces.remove(it.key)
                }
                is SchemaCreatedEvent -> {
                    val schema = Schema(it.key.schema, it.schemaType)
                    schemas[it.key] = schema
                }
                is SchemaDeletedEvent -> {
                    schemas.remove(it.key)
                }
                is VersionCreatedEvent -> {
                    val version = Version(it.key.version)
                    versions[it.key] = version
                }
                is VersionDeletedEvent -> {
                    versions.remove(it.key)
                }
                is ArtifactCreatedEvent -> {
                    val artifact = Artifact(it.key.filename, it.mediaType, it.path)
                    artifacts[it.key] = artifact
                }
                is ArtifactDeletedEvent -> {
                    artifacts.remove(it.key)
                }
                else -> {
                    log.warn("Unhandled event: $it")
                }
            }
        }
    }

    fun getNamespaces() = namespaces.values.toSet()

    fun getNamespace(key: NamespaceKey) = namespaces[key]

    fun getSchemas(key: NamespaceKey) = schemas.entries.filter { it.key.namespace == key.namespace }.map { it.value }.toSet()

    fun getSchema(key: SchemaKey) = schemas[key]

    fun getVersions(key: SchemaKey) = versions.entries.filter { it.key.namespace == key.namespace && it.key.schema == key.schema }.map { it.value }.toSet()

    fun getVersion(key: VersionKey) = versions[key]

    fun getArtifacts(key: VersionKey) = artifacts.entries.filter { it.key.namespace == key.namespace && it.key.schema == key.schema && it.key.version == key.version }.map { it.value }.toSet()

    fun getArtifact(key: ArtifactKey) = artifacts[key]

    fun getVersionRange(key: SchemaKey, rangeStart: String?, rangeStop: String?) = {
        val filtered = versions.entries.filter { it.key.namespace == key.namespace && it.key.schema == key.schema }.map { it.value }
        VersionRangeQuery(filtered).getVersionRange(rangeStart, rangeStop)
    }
}