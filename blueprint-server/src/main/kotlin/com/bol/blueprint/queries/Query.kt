package com.bol.blueprint.queries

import com.bol.blueprint.domain.*
import mu.KotlinLogging

class Query : Sink, Resettable {
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

    override fun <T> getHandler(): suspend (Event.Metadata, T) -> Unit {
        return { _, event ->
            log.debug("Received: $event")
            when (event) {
                is NamespaceCreatedEvent -> {
                    namespaces[event.key] = Namespace(event.key.namespace)
                }
                is NamespaceDeletedEvent -> {
                    namespaces.remove(event.key)
                }
                is SchemaCreatedEvent -> {
                    val schema = Schema(event.key.schema, event.schemaType)
                    schemas[event.key] = schema
                }
                is SchemaDeletedEvent -> {
                    schemas.remove(event.key)
                }
                is VersionCreatedEvent -> {
                    val version = Version(event.key.version)
                    versions[event.key] = version
                }
                is VersionDeletedEvent -> {
                    versions.remove(event.key)
                }
                is ArtifactCreatedEvent -> {
                    val artifact = Artifact(event.key.filename, event.mediaType, event.path)
                    artifacts[event.key] = artifact
                }
                is ArtifactDeletedEvent -> {
                    artifacts.remove(event.key)
                }
                else -> {
                    log.warn("Unhandled event: $event")
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

    /*fun getVersionRange(key: SchemaKey, rangeStart: String?, rangeStop: String?) = {
        VersionRangeQuery(getFilteredVersions(key), Semver.SemverType.IVY).getVersionRange(rangeStart, rangeStop)
    }

    private fun getFilteredVersions(key: SchemaKey): Collection<Version> =
            versions.entries.filter { it.key.namespace == key.namespace && it.key.schema == key.schema }.map { it.value }*/
}