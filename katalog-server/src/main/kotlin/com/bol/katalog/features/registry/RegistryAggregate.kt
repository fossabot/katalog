package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.CommandFailure
import com.bol.katalog.cqrs.CqrsAggregate
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.store.BlobStore
import com.vdurmont.semver4j.Semver
import org.springframework.stereotype.Component

@Component
internal class RegistryAggregate(
    context: AggregateContext,
    permissionManager: PermissionManager,
    private val blobStore: BlobStore
) : CqrsAggregate<Registry>(context, Registry(context, permissionManager)) {
    override fun getCommandHandler() = commandHandler {
        handle<CreateNamespaceCommand> {
            if (state.namespaces.values.any {
                    it.name == command.name || it.id == command.id
                }) fail(CommandFailure.Conflict("Namespace already exists: $command.name"))

            event(NamespaceCreatedEvent(command.id, command.groupId, command.name))
        }

        handle<DeleteNamespaceCommand> {
            if (!state.namespaces.containsKey(command.id)) fail(CommandFailure.NotFound("Namespace id not found: $command.id"))

            state.schemas
                .filterValues { it.namespace.id == this.command.id }
                .keys
                .forEach {
                    require(DeleteSchemaCommand(it))
                }

            event(NamespaceDeletedEvent(command.id))
        }

        handle<CreateSchemaCommand> {
            if (state.namespaces.values.none {
                    it.id == command.namespaceId
                }) fail(CommandFailure.NotFound("Unknown namespace id: ${command.namespaceId}"))
            if (state.schemas.values.any {
                    it.namespace.id == command.namespaceId && it.name == command.name
                }) fail(CommandFailure.Conflict("Schema already exists: ${command.name}"))

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
            if (!state.schemas.containsKey(command.id)) fail(CommandFailure.NotFound("Unknown schema id: $command.id"))

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
            state.namespaces[event.id] = Namespace(event.id, event.name, event.groupId, metadata.timestamp)
        }
        handle<NamespaceDeletedEvent> {
            state.namespaces.remove(event.id)
        }

        handle<SchemaCreatedEvent> {
            val namespace = state.getNamespace(event.namespaceId)
            val schema = Schema(event.id, metadata.timestamp, event.name, event.schemaType, namespace)
            state.schemas[event.id] = schema
        }
        handle<SchemaDeletedEvent> {
            state.schemas.remove(event.id)
        }

        handle<VersionCreatedEvent> {
            val schema = state.getSchema(event.schemaId)
            val version = Version(
                event.id,
                metadata.timestamp,
                Semver(event.version, schema.type.toSemVerType()),
                schema
            )
            state.versions[event.id] = version
        }
        handle<VersionDeletedEvent> {
            state.versions.remove(event.id)
        }

        handle<ArtifactCreatedEvent> {
            val version = state.getVersion(event.versionId)

            val artifact = Artifact(event.id, event.filename, event.data.size, event.mediaType, version)
            state.artifacts[event.id] = artifact
        }
        handle<ArtifactDeletedEvent> {
            state.artifacts.remove(event.id)
        }
    }
}