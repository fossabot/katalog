package com.bol.blueprint

import kotlinx.coroutines.experimental.channels.SendChannel
import mu.KLogging

class Query : Sink<Event> {
    companion object : KLogging()

    private val namespaces = mutableMapOf<NamespaceKey, Namespace>()
    private val schemas = mutableMapOf<SchemaKey, Schema>()
    private val versions = mutableMapOf<VersionKey, Version>()

    private val sendChannel: SendChannel<Event> = eventHandler {
        logger.debug("Received: $it")
        when (it) {
            is NamespaceCreatedEvent -> {
                namespaces[it.key] = Namespace(it.key.namespace)
            }
            is SchemaCreatedEvent -> {
                val schema = Schema(it.key.schema, it.schemaType)
                schemas[it.key] = schema
            }
            is VersionCreatedEvent -> {
                val version = Version(it.key.version)
                versions[it.key] = version
            }
        }
    }

    override fun getSink(): SendChannel<Event> = sendChannel

    fun getNamespaces() = namespaces.values.toSet()

    fun getNamespace(key: NamespaceKey) = namespaces[key]

    fun getSchemas(key: NamespaceKey) = schemas.entries.filter { it.key.namespace == key.namespace }.map { it.value }.toSet()

    fun getSchema(key: SchemaKey) = schemas[key]

    fun getVersions(key: SchemaKey) = versions.entries.filter { it.key.namespace == key.namespace && it.key.schema == key.schema }.map { it.value }.toSet()

    fun getVersion(key: VersionKey) = versions[key]
}