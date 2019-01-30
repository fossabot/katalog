package com.bol.katalog.features.registry.support

import com.bol.katalog.features.registry.*
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.bol.katalog.support.AggregateTester

object RegistryTester {
    internal fun get() = AggregateTester
        .of { ctx, permissionManager ->
            RegistryAggregate(ctx, permissionManager, InMemoryBlobStore())
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
    CreateSchemaCommand(namespace.id, id, name, type)

fun Schema.created() =
    SchemaCreatedEvent(namespace.id, id, name, type)
fun Schema.delete() = DeleteSchemaCommand(id)
fun Schema.deleted() = SchemaDeletedEvent(id)
fun Version.create() =
    CreateVersionCommand(schema.id, id, semVer.toString())

fun Version.created() =
    VersionCreatedEvent(schema.id, id, semVer.toString())
fun Version.delete() = DeleteVersionCommand(id)
fun Version.deleted() = VersionDeletedEvent(id)
fun Artifact.create(data: ByteArray) =
    CreateArtifactCommand(version.id, id, filename, mediaType, data)

fun Artifact.created(data: ByteArray) =
    ArtifactCreatedEvent(version.id, id, filename, mediaType, data)
fun Artifact.delete() = DeleteArtifactCommand(id)
fun Artifact.deleted() = ArtifactDeletedEvent(id)