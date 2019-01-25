package com.bol.katalog.features.registry

/**
 * These extension methods can be used to simplify the creation of commands/events in testing
 */
fun Namespace.create() = CreateNamespaceCommand(id, groupId, name)

fun Namespace.created() = NamespaceCreatedEvent(id, groupId, name)
fun Namespace.delete() = DeleteNamespaceCommand(id)
fun Namespace.deleted() = NamespaceDeletedEvent(id)
fun Schema.create(owner: Namespace) = CreateSchemaCommand(owner.id, id, name, type)
fun Schema.created(owner: Namespace) = SchemaCreatedEvent(owner.id, id, name, type)
fun Schema.delete() = DeleteSchemaCommand(id)
fun Schema.deleted() = SchemaDeletedEvent(id)
fun Version.create(owner: Schema) = CreateVersionCommand(owner.id, id, semVer.toString())
fun Version.created(owner: Schema) = VersionCreatedEvent(owner.id, id, semVer.toString())
fun Version.delete() = DeleteVersionCommand(id)
fun Version.deleted() = VersionDeletedEvent(id)
fun Artifact.create(owner: Version, data: ByteArray) = CreateArtifactCommand(owner.id, id, filename, mediaType, data)
fun Artifact.created(owner: Version, data: ByteArray) = ArtifactCreatedEvent(owner.id, id, filename, mediaType, data)
fun Artifact.delete() = DeleteArtifactCommand(id)
fun Artifact.deleted() = ArtifactDeletedEvent(id)