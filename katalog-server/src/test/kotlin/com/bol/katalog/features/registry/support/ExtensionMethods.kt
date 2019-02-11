package com.bol.katalog.features.registry.support

import com.bol.katalog.features.registry.*
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.bol.katalog.support.AggregateTester

object RegistryTester {
    internal fun get() = AggregateTester
        .of { ctx, permissionManager ->
            listOf(RegistryAggregate(ctx, permissionManager, InMemoryBlobStore()))
        }
}

/**
 * These extension methods can be used to simplify the creation of commands/events in testing
 */
fun Namespace.create() =
    CreateNamespaceCommand(id, groupId, name)

fun Namespace.created() =
    NamespaceCreatedEvent(id, groupId, name)

fun Namespace.delete() = DeleteNamespaceCommand(id)
fun Namespace.deleted() = NamespaceDeletedEvent(id)
fun Schema.create() =
    CreateSchemaCommand(namespaceId, id, name, type)

fun Schema.created() =
    SchemaCreatedEvent(namespaceId, id, name, type)

fun Schema.delete() = DeleteSchemaCommand(id)
fun Schema.deleted() = SchemaDeletedEvent(id)
fun Version.create() =
    CreateVersionCommand(schemaId, id, version)

fun Version.created() =
    VersionCreatedEvent(schemaId, id, version)

fun Version.delete() = DeleteVersionCommand(id)
fun Version.deleted() = VersionDeletedEvent(id)
fun Artifact.create(data: ByteArray) =
    CreateArtifactCommand(versionId, id, filename, mediaType, data)

fun Artifact.created(data: ByteArray) =
    ArtifactCreatedEvent(versionId, id, filename, data.size, mediaType)

fun Artifact.delete() = DeleteArtifactCommand(id)
fun Artifact.deleted() = ArtifactDeletedEvent(id)