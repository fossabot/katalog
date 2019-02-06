package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.CommandFailure
import com.bol.katalog.cqrs.CqrsAggregate
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.requirePermissionOrForbidden
import com.bol.katalog.store.BlobStore
import com.bol.katalog.users.GroupPermission
import org.springframework.stereotype.Component

@Component
internal class RegistryAggregate(
    context: AggregateContext,
    private val permissionManager: PermissionManager,
    private val blobStore: BlobStore
) : CqrsAggregate<Registry>(context, Registry(context, permissionManager)) {
    override fun getCommandHandler() = commandHandler {
        handle<CreateNamespaceCommand> {
            if (state.namespaces.exists(namespaceId = command.id, namespace = command.name)) {
                fail(CommandFailure.Conflict("Namespace already exists: ${command.name}"))
            }
            permissionManager.requirePermissionOrForbidden(command.groupId, GroupPermission.CREATE)

            event(NamespaceCreatedEvent(command.id, command.groupId, command.name))
        }

        handle<DeleteNamespaceCommand> {
            val namespace = state.namespaces.getById(command.id)
            permissionManager.requirePermissionOrForbidden(namespace.groupId, GroupPermission.DELETE)

            state.schemas
                .getByNamespaceIds(listOf(this.command.id))
                .forEach {
                    require(DeleteSchemaCommand(it.id))
                }

            event(NamespaceDeletedEvent(command.id))
        }

        handle<CreateSchemaCommand> {
            val namespace = state.namespaces.getById(command.namespaceId)
            if (state.schemas.exists(namespaceId = command.namespaceId, schema = command.name)) {
                fail(CommandFailure.Conflict("Schema already exists: ${command.name}"))
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

        handle<DeleteSchemaCommand> {
            val schema = state.schemas.getById(command.id)
            permissionManager.requirePermissionOrForbidden(schema.namespace.groupId, GroupPermission.DELETE)

            state.versions.getAll(schema.id)
                .filter { it.schema.id == this.command.id }
                .forEach {
                    require(DeleteVersionCommand(it.id))
                }

            event(SchemaDeletedEvent(command.id))
        }

        handle<CreateVersionCommand> {
            if (state.versions.exists(schemaId = command.schemaId, version = command.version)) {
                fail(CommandFailure.Conflict("Version already exists: ${command.version}"))
            }

            event(VersionCreatedEvent(command.schemaId, command.id, command.version))
        }

        handle<DeleteVersionCommand> {
            val version = state.versions.getById(command.id)
            permissionManager.requirePermissionOrForbidden(version.schema.namespace.groupId, GroupPermission.DELETE)

            state.artifacts.getAll(listOf(command.id))
                .forEach {
                    require(DeleteArtifactCommand(it.id))
                }

            event(VersionDeletedEvent(command.id))
        }

        handle<CreateArtifactCommand> {
            if (state.artifacts.exists(versionId = command.versionId, filename = command.filename)) {
                fail(CommandFailure.Conflict("Artifact already exists: ${command.filename}"))
            }

            val path = getBlobStorePath(command.id)
            blobStore.store(path, command.data)

            event(
                ArtifactCreatedEvent(
                    command.versionId,
                    command.id,
                    command.filename,
                    command.mediaType,
                    command.data
                )
            )
        }

        handle<DeleteArtifactCommand> {
            val artifact = state.artifacts.getById(command.id)
            permissionManager.requirePermissionOrForbidden(
                artifact.version.schema.namespace.groupId,
                GroupPermission.DELETE
            )

            val path = getBlobStorePath(command.id)
            blobStore.delete(path)

            event(ArtifactDeletedEvent(command.id))
        }
    }

    override fun getEventHandler() = eventHandler {
        handle<NamespaceCreatedEvent> {
            state.namespaces.add(Namespace(event.id, event.name, event.groupId, metadata.timestamp))
        }
        handle<NamespaceDeletedEvent> {
            state.namespaces.removeById(event.id)
        }

        handle<SchemaCreatedEvent> {
            val namespace = state.namespaces.getById(event.namespaceId)
            val schema = Schema(event.id, metadata.timestamp, event.name, event.schemaType, namespace)
            state.schemas.add(schema)
        }
        handle<SchemaDeletedEvent> {
            state.schemas.removeById(event.id)
        }

        handle<VersionCreatedEvent> {
            val schema = state.schemas.getById(event.schemaId)
            val version = Version(
                event.id,
                metadata.timestamp,
                event.version,
                schema
            )
            state.versions.add(version)
        }
        handle<VersionDeletedEvent> {
            state.versions.removeById(event.id)
        }

        handle<ArtifactCreatedEvent> {
            val version = state.versions.getById(event.versionId)
            val artifact = Artifact(event.id, event.filename, event.data.size, event.mediaType, version)
            state.artifacts.add(artifact)
        }
        handle<ArtifactDeletedEvent> {
            state.artifacts.removeById(event.id)
        }
    }
}