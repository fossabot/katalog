package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AbstractAggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ConflictException
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.requirePermissionOrForbidden
import com.bol.katalog.security.requirePermissionOrForbiddenBy
import com.bol.katalog.store.BlobStore
import com.bol.katalog.users.GroupPermission
import org.springframework.stereotype.Component

@Component
final class RegistryAggregate(
    context: AggregateContext,
    permissionManager: PermissionManager,
    blobStore: BlobStore
) : AbstractAggregate(context) {
    init {
        setup {
            command<CreateNamespaceCommand> {
                if (namespaces.exists(namespaceId = command.id, namespace = command.name)) {
                    throw ConflictException("Namespace already exists: ${command.name}")
                }
                permissionManager.requirePermissionOrForbidden(command.groupId, GroupPermission.CREATE)

                event(NamespaceCreatedEvent(command.id, command.groupId, command.name))
            }

            command<DeleteNamespaceCommand> {
                val namespace = namespaces.getById(command.id)
                permissionManager.requirePermissionOrForbiddenBy(namespace, GroupPermission.DELETE)

                schemas
                    .getByNamespaceIds(listOf(this.command.id))
                    .forEach {
                        require(DeleteSchemaCommand(it.id))
                    }

                event(NamespaceDeletedEvent(command.id))
            }

            command<CreateSchemaCommand> {
                val namespace = namespaces.getById(command.namespaceId)
                if (schemas.exists(namespaceId = command.namespaceId, schema = command.name)) {
                    throw ConflictException("Schema already exists: ${command.name}")
                }
                permissionManager.requirePermissionOrForbidden(namespace.groupId, GroupPermission.CREATE)

                event(
                    SchemaCreatedEvent(
                        command.namespaceId,
                        command.id,
                        command.name,
                        command.schemaType
                    )
                )
            }

            command<DeleteSchemaCommand> {
                val schema = schemas.getById(command.id)
                permissionManager.requirePermissionOrForbiddenBy(schema, GroupPermission.DELETE)

                versions.getAll(schema.id)
                    .filter { it.schemaId == this.command.id }
                    .forEach {
                        require(DeleteVersionCommand(it.id))
                    }

                event(SchemaDeletedEvent(command.id))
            }

            command<CreateVersionCommand> {
                if (versions.exists(schemaId = command.schemaId, version = command.version)) {
                    val schema = schemas.getById(command.schemaId)
                    val existing = versions.getByVersion(command.schemaId, command.version)
                    val existingSemVer = existing.toSemVer(schema)
                    if (existingSemVer.isStable) {
                        throw ConflictException("Version already exists: ${command.version}")
                    } else {
                        // Version already exists, but it's an unstable version. We can replace it.
                        require(DeleteVersionCommand(existing.id))
                        event(VersionReplacedEvent(command.schemaId, command.id, command.version, existing.id))
                    }
                } else {
                    // This is a new version
                    event(VersionCreatedEvent(command.schemaId, command.id, command.version))
                }
            }

            command<DeleteVersionCommand> {
                val version = versions.getById(command.id)
                permissionManager.requirePermissionOrForbiddenBy(version, GroupPermission.DELETE)

                artifacts.getByVersion(command.id)
                    .forEach {
                        require(DeleteArtifactCommand(it.id))
                    }

                event(VersionDeletedEvent(command.id))
            }

            command<CreateArtifactCommand> {
                if (artifacts.exists(versionId = command.versionId, filename = command.filename)) {
                    throw ConflictException("Artifact already exists: ${command.filename}")
                }

                val path = getBlobStorePath(command.id)
                blobStore.store(path, command.data)

                event(
                    ArtifactCreatedEvent(
                        command.versionId,
                        command.id,
                        command.filename,
                        command.data.size,
                        command.mediaType
                    )
                )
            }

            command<DeleteArtifactCommand> {
                val artifact = artifacts.getById(command.id)
                permissionManager.requirePermissionOrForbiddenBy(
                    artifact,
                    GroupPermission.DELETE
                )

                val path = getBlobStorePath(command.id)
                blobStore.delete(path)

                event(ArtifactDeletedEvent(command.id))
            }

            event<NamespaceCreatedEvent> {
                namespaces.add(Namespace(event.id, event.name, event.groupId, timestamp))
            }

            event<NamespaceDeletedEvent> {
                namespaces.removeById(event.id)
            }

            event<SchemaCreatedEvent> {
                val groupId = namespaces.getById(event.namespaceId).groupId
                val schema = Schema(event.id, groupId, event.namespaceId, timestamp, event.name, event.schemaType)
                schemas.add(schema)
            }

            event<SchemaDeletedEvent> {
                schemas.removeById(event.id)
            }

            event<VersionCreatedEvent> {
                val schema = schemas.getById(event.schemaId)
                val namespace = namespaces.getById(schema.namespaceId)
                val version = Version(
                    event.id,
                    namespace.groupId,
                    event.schemaId,
                    timestamp,
                    event.version
                )
                versions.add(version)
            }

            event<VersionReplacedEvent> {
                val schema = schemas.getById(event.schemaId)
                val namespace = namespaces.getById(schema.namespaceId)
                val version = Version(
                    event.id,
                    namespace.groupId,
                    event.schemaId,
                    timestamp,
                    event.version
                )
                versions.add(version)
            }

            event<VersionDeletedEvent> {
                versions.removeById(event.id)
            }

            event<ArtifactCreatedEvent> {
                val version = versions.getById(event.versionId)
                val schema = schemas.getById(version.schemaId)
                val namespace = namespaces.getById(schema.namespaceId)
                val artifact = Artifact(
                    event.id,
                    namespace.groupId,
                    event.versionId,
                    event.filename,
                    event.filesize,
                    event.mediaType
                )
                artifacts.add(artifact)
            }

            event<ArtifactDeletedEvent> {
                artifacts.removeById(event.id)
            }
        }
    }

    internal val namespaces = NamespaceRegistry(context, permissionManager)
    internal val schemas = SchemaRegistry(context, permissionManager)
    internal val versions = VersionRegistry(context, permissionManager, this)
    internal val artifacts = ArtifactRegistry(context, permissionManager)

    override suspend fun reset() {
        namespaces.reset()
        schemas.reset()
        versions.reset()
        artifacts.reset()
    }
}