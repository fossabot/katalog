package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.CommandFailure
import com.bol.katalog.cqrs.CqrsAggregate
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.requirePermissionOrForbidden
import com.bol.katalog.store.BlobStore
import com.bol.katalog.users.GroupPermission
import com.vdurmont.semver4j.Semver
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

            state.versions
                .filterValues { it.schema.id == this.command.id }
                .keys
                .forEach {
                    require(DeleteVersionCommand(it))
                }

            event(SchemaDeletedEvent(command.id))
        }

        handle<CreateVersionCommand> {
            if (state.versions.values.any {
                    it.schema.id == command.schemaId && it.semVer.value == command.version
                }) fail(CommandFailure.Conflict("Version already exists: ${command.version}"))

            event(VersionCreatedEvent(command.schemaId, command.id, command.version))
        }

        handle<DeleteVersionCommand> {
            if (!state.versions.containsKey(command.id)) fail(CommandFailure.NotFound("Unknown version id: $command.id"))

            state.artifacts
                .filterValues { it.version.id == this.command.id }
                .keys
                .forEach {
                    require(DeleteArtifactCommand(it))
                }

            event(VersionDeletedEvent(command.id))
        }

        handle<CreateArtifactCommand> {
            if (state.artifacts.values.any {
                    it.version.id == command.versionId && it.filename == command.filename
                }) fail(CommandFailure.Conflict("Artifact already exists: ${command.filename}"))


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
            if (!state.artifacts.containsKey(command.id)) fail(CommandFailure.NotFound("Unknown artifact id: $command.id"))

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
            state.versionsBySchema[schema.id] = mutableListOf()
        }
        handle<SchemaDeletedEvent> {
            state.schemas.removeById(event.id)
            state.versionsBySchema.remove(event.id)
        }

        handle<VersionCreatedEvent> {
            val schema = state.schemas.getById(event.schemaId)
            val version = Version(
                event.id,
                metadata.timestamp,
                Semver(event.version, schema.type.toSemVerType()),
                schema
            )
            state.versions[event.id] = version
            state.versionsBySchema[event.schemaId]!!.add(version)
            state.updateMajorCurrentVersions(schema.id)

            state.artifactsByVersion[event.id] = mutableListOf()
        }
        handle<VersionDeletedEvent> {
            val version = state.getVersion(event.id)
            state.versions.remove(event.id)
            state.versionsBySchema[version.schema.id]!!.remove(version)
            state.updateMajorCurrentVersions(version.schema.id)

            state.artifactsByVersion.remove(event.id)
        }

        handle<ArtifactCreatedEvent> {
            val version = state.getVersion(event.versionId)

            val artifact = Artifact(event.id, event.filename, event.data.size, event.mediaType, version)
            state.artifacts[event.id] = artifact
            state.artifactsByVersion[event.versionId]!!.add(artifact)
        }
        handle<ArtifactDeletedEvent> {
            val artifact = state.getArtifact(event.id)
            state.artifacts.remove(event.id)
            state.artifactsByVersion[artifact.version.id]!!.remove(artifact)
        }
    }
}