package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ConflictFailure
import com.bol.katalog.cqrs.NotFoundFailure
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.store.BlobStore
import com.vdurmont.semver4j.Semver
import org.springframework.stereotype.Component

@Component
class RegistryAggregate(
    context: AggregateContext,
    security: SecurityAggregate,
    private val blobStore: BlobStore
) : Aggregate<RegistryState>(context, RegistryState(context, security)) {
    override fun getCommandHandler() = commandHandler {
        handle<CreateNamespaceCommand> {
            if (state.namespaces.values.any {
                    it.name == command.name || it.id == command.id
                }) fail(ConflictFailure())

            event(NamespaceCreatedEvent(command.id, command.groupId, command.name))
        }

        handle<DeleteNamespaceCommand> {
            if (!state.namespaces.containsKey(command.id)) fail(NotFoundFailure())

            state.schemas
                .filterValues { it.namespaceId == this.command.id }
                .keys
                .forEach {
                    require(DeleteSchemaCommand(it))
                }

            event(NamespaceDeletedEvent(command.id))
        }

        handle<CreateSchemaCommand> {
            if (state.schemas.values.any {
                    it.namespaceId == command.namespaceId && it.schema.name == command.name
                }) fail(ConflictFailure())

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
            if (!state.schemas.containsKey(command.id)) fail(NotFoundFailure())

            state.versions
                .filterValues { it.schemaId == this.command.id }
                .keys
                .forEach {
                    require(DeleteVersionCommand(it))
                }

            event(SchemaDeletedEvent(command.id))
        }

        handle<CreateVersionCommand> {
            if (state.versions.values.any {
                    it.schemaId == command.schemaId && it.version.semVer.value == command.version
                }) fail(ConflictFailure())

            event(VersionCreatedEvent(command.schemaId, command.id, command.version))
        }

        handle<DeleteVersionCommand> {
            if (!state.versions.containsKey(command.id)) fail(NotFoundFailure())

            state.artifacts
                .filterValues { it.versionId == this.command.id }
                .keys
                .forEach {
                    require(DeleteArtifactCommand(it))
                }

            event(VersionDeletedEvent(command.id))
        }

        handle<CreateArtifactCommand> {
            if (state.artifacts.values.any {
                    it.versionId == command.versionId && it.artifact.filename == command.filename
                }) fail(ConflictFailure())


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
            if (!state.artifacts.containsKey(command.id)) fail(NotFoundFailure())

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
            val schema = Schema(event.id, metadata.timestamp, event.name, event.schemaType)
            state.schemas[event.id] = RegistryState.SchemaEntry(event.namespaceId, event.id, schema)
        }
        handle<SchemaDeletedEvent> {
            state.schemas.remove(event.id)
        }

        handle<VersionCreatedEvent> {
            val namespaceId = state.getSchemaNamespaceId(event.schemaId)
            val schema = state.getSchema(event.schemaId)
            val version = Version(
                event.id,
                metadata.timestamp,
                Semver(event.version, schema.type.toSemVerType())
            )
            state.versions[event.id] = RegistryState.VersionEntry(namespaceId, event.schemaId, event.id, version)
        }
        handle<VersionDeletedEvent> {
            state.versions.remove(event.id)
        }

        handle<ArtifactCreatedEvent> {
            val schemaId = state.getVersionSchemaId(event.versionId)
            val namespaceId = state.getSchemaNamespaceId(schemaId)

            val artifact = Artifact(event.id, event.filename, event.data.size, event.mediaType)
            state.artifacts[event.id] = RegistryState.ArtifactEntry(
                namespaceId,
                schemaId,
                event.versionId,
                artifact
            )
        }
        handle<ArtifactDeletedEvent> {
            state.artifacts.remove(event.id)
        }
    }
}