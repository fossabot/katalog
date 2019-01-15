package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.ConflictException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.CommandHandlerBuilder
import com.bol.katalog.cqrs.events.EventHandler
import com.bol.katalog.cqrs.events.EventHandlerBuilder
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.store.BlobStore
import com.bol.katalog.users.GroupPermission
import com.vdurmont.semver4j.Semver
import org.springframework.stereotype.Component

@Component
class RegistryAggregate(
    private val security: SecurityAggregate,
    private val blobStore: BlobStore
) : EventHandler, CommandHandler, Resettable {
    private val namespaces = mutableMapOf<NamespaceId, Namespace>()
    private val schemas = mutableMapOf<SchemaId, SchemaEntry>()
    private val versions = mutableMapOf<VersionId, VersionEntry>()
    private val artifacts = mutableMapOf<ArtifactId, ArtifactEntry>()

    data class SchemaEntry(val namespaceId: NamespaceId, val schemaId: SchemaId, val schema: Schema)

    data class VersionEntry(
        val namespaceId: NamespaceId,
        val schemaId: SchemaId,
        val versionId: VersionId,
        val version: Version
    )

    data class ArtifactEntry(
        val namespaceId: NamespaceId,
        val schemaId: SchemaId,
        val versionId: VersionId,
        val artifact: Artifact
    )

    override val eventHandler
        get() = EventHandlerBuilder.handleEvents {
            handle<NamespaceCreatedEvent> {
                namespaces[it.id] = Namespace(it.id, it.name, it.groupId, metadata.timestamp)
            }
            handle<NamespaceDeletedEvent> {
                namespaces.remove(it.id)
            }

            handle<SchemaCreatedEvent> {
                val schema = Schema(it.id, metadata.timestamp, it.name, it.schemaType)
                schemas[it.id] = SchemaEntry(it.namespaceId, it.id, schema)
            }
            handle<SchemaDeletedEvent> {
                schemas.remove(it.id)
            }

            handle<VersionCreatedEvent> {
                val namespaceId = getSchemaNamespaceId(it.schemaId)
                val schema = getSchema(it.schemaId)
                val version = Version(
                    it.id,
                    metadata.timestamp,
                    Semver(it.version, schema.type.toSemVerType())
                )
                versions[it.id] = VersionEntry(namespaceId, it.schemaId, it.id, version)
            }
            handle<VersionDeletedEvent> {
                versions.remove(it.id)
            }

            handle<ArtifactCreatedEvent> {
                val schemaId = getVersionSchemaId(it.versionId)
                val namespaceId = getSchemaNamespaceId(schemaId)

                val artifact = Artifact(it.id, it.filename, it.data.size, it.mediaType)
                artifacts[it.id] = ArtifactEntry(
                    namespaceId,
                    schemaId,
                    it.versionId,
                    artifact
                )
            }
            handle<ArtifactDeletedEvent> {
                artifacts.remove(it.id)
            }
        }

    override val commandHandler
        get() = CommandHandlerBuilder.handleCommands {
            handle<CreateNamespaceCommand> {
                if (namespaces.values.any {
                        it.name == command.name || it.id == command.id
                    }) throw ConflictException()

                event(NamespaceCreatedEvent(command.id, command.groupId, command.name))
                complete()
            }

            handle<DeleteNamespaceCommand> {
                if (!namespaces.containsKey(command.id)) throw NotFoundException()

                schemas
                    .filterValues { it.namespaceId == this.command.id }
                    .keys
                    .forEach {
                        require(DeleteSchemaCommand(it))
                    }

                event(NamespaceDeletedEvent(command.id))
                complete()
            }

            handle<CreateSchemaCommand> {
                if (schemas.values.any {
                        it.namespaceId == command.namespaceId && it.schema.name == command.name
                    }) throw ConflictException()

                event(
                    SchemaCreatedEvent(
                        command.namespaceId,
                        command.id,
                        command.name,
                        command.schemaType
                    )
                )
                complete()
            }

            handle<DeleteSchemaCommand> {
                if (!schemas.containsKey(command.id)) throw NotFoundException()

                versions
                    .filterValues { it.schemaId == this.command.id }
                    .keys
                    .forEach {
                        require(DeleteVersionCommand(it))
                    }

                event(SchemaDeletedEvent(command.id))
                complete()
            }

            handle<CreateVersionCommand> {
                if (versions.values.any {
                        it.schemaId == command.schemaId && it.version.semVer.value == command.version
                    }) throw ConflictException()

                event(VersionCreatedEvent(command.schemaId, command.id, command.version))
                complete()
            }

            handle<DeleteVersionCommand> {
                if (!versions.containsKey(command.id)) throw NotFoundException()

                artifacts
                    .filterValues { it.versionId == this.command.id }
                    .keys
                    .forEach {
                        require(DeleteArtifactCommand(it))
                    }

                event(VersionDeletedEvent(command.id))
                complete()
            }

            handle<CreateArtifactCommand> {
                if (artifacts.values.any {
                        it.versionId == command.versionId && it.artifact.filename == command.filename
                    }) throw ConflictException()


                effect {
                    val path = getBlobStorePath(command.id)
                    blobStore.store(path, command.data)
                }

                event(
                    ArtifactCreatedEvent(
                        command.versionId,
                        command.id,
                        command.filename,
                        command.mediaType,
                        command.data
                    )
                )

                complete()
            }

            handle<DeleteArtifactCommand> {
                if (!artifacts.containsKey(command.id)) throw NotFoundException()

                effect {
                    val path = getBlobStorePath(command.id)
                    blobStore.delete(path)
                }

                event(ArtifactDeletedEvent(command.id))
                complete()
            }
        }

    override fun reset() {
        namespaces.clear()
        schemas.clear()
        versions.clear()
        artifacts.clear()
    }

    /**
     * Get all available namespaces
     */
    suspend fun getNamespaces(): Collection<Namespace> = filteredForUser(namespaces.values)

    /**
     * Get namespace based on id
     */
    suspend fun getNamespace(namespaceId: NamespaceId): Namespace {
        val filtered = filteredForUser(listOfNotNull(namespaces[namespaceId]))
        return filtered.singleOrNull()
            ?: throw NotFoundException("Could not find namespace with id: $namespaceId")
    }

    suspend fun findNamespace(namespace: String): Namespace {
        val filtered = filteredForUser(namespaces.values)
        return filtered.firstOrNull { it.name == namespace }
            ?: throw NotFoundException("Could not find namespace: $namespace")
    }

    fun findUnauthorizedNamespace(namespace: String) = namespaces.values
        .firstOrNull { it.name == namespace }
        ?: throw NotFoundException("Could not find namespace: $namespace")

    // Filter the namespaces based on user, or remove them all if the user is null
    private suspend fun filteredForUser(namespaces: Collection<Namespace>): Collection<Namespace> {
        return CoroutineUserContext.get()?.let { user ->
            return namespaces.filter {
                security.hasPermission(user, it.groupId, GroupPermission.READ)
            }
        } ?: emptyList()
    }

    /**
     * Get all available schemas
     */
    suspend fun getSchemas(): Collection<Schema> = schemas.values.map { it.schema }

    /**
     * Get all schemas for the specified namespaces
     */
    suspend fun getSchemas(namespaceIds: Collection<NamespaceId>): Collection<Schema> = schemas.filter {
        namespaceIds.any { id ->
            it.value.namespaceId == id
        }
    }.map { it.value.schema }

    /**
     * Get schema based on id
     */
    suspend fun getSchema(schemaId: SchemaId) =
        schemas[schemaId]?.schema ?: throw NotFoundException("Could not find schema with id: $schemaId")

    suspend fun getSchemaNamespaceId(schemaId: SchemaId) = schemas[schemaId]?.namespaceId
        ?: throw NotFoundException("Could not find schema with id: $schemaId")

    suspend fun findSchema(namespaceId: NamespaceId, schema: String) =
        schemas.values
            .filter {
                it.namespaceId == namespaceId && it.schema.name == schema
            }
            .map { it.schema }
            .singleOrNull()
            ?: throw NotFoundException("Could not find schema: $schema in namespace with id: $namespaceId")

    suspend fun getVersions(schemaId: SchemaId) = versions.filter {
        it.value.schemaId == schemaId
    }.map { it.value.version }

    /**
     * Get version based on id
     */
    suspend fun getVersion(versionId: VersionId) =
        versions[versionId]?.version ?: throw NotFoundException("Could not find version with id: $versionId")

    suspend fun getVersionSchemaId(versionId: VersionId) = versions[versionId]?.schemaId
        ?: throw NotFoundException("Could not find version with id: $versionId")

    /**
     * Get the current major versions
     */
    suspend fun getCurrentMajorVersions(versions: Collection<Version>): Collection<Version> {
        return versions
            .sortedByDescending { it.semVer }
            .groupBy { it.semVer.major }
            .mapValues { entry ->
                val items = entry.value
                if (items.size == 1) {
                    items
                } else {
                    // Find first stable version
                    val stableVersion = items.first { it.semVer.isStable }
                    listOf(stableVersion)
                }
            }
            .flatMap { it.value }
    }

    /**
     * Is this a current version (i.e. the latest stable version of a major version)?
     */
    suspend fun isCurrent(schemaId: SchemaId, version: Version) =
        getCurrentMajorVersions(getVersions(schemaId)).contains(version)

    suspend fun findVersion(namespaceId: NamespaceId, schemaId: SchemaId, version: String) = versions.values
        .filter {
            it.namespaceId == namespaceId && it.schemaId == schemaId && it.version.semVer.value == version
        }
        .map { it.version }
        .singleOrNull()
        ?: throw NotFoundException("Could not find version: $version in schema with id: $schemaId and namespace with id: $namespaceId")

    suspend fun getArtifacts() = artifacts.values.map { it.artifact }

    suspend fun getArtifacts(versionIds: Collection<VersionId>) = artifacts.filter {
        versionIds.any { id ->
            it.value.versionId == id
        }
    }.map { it.value.artifact }

    /**
     * Get artifact based on id
     */
    suspend fun getArtifact(artifactId: ArtifactId) =
        artifacts[artifactId]?.artifact ?: throw NotFoundException("Could not find artifact with id: $artifactId")

    suspend fun getArtifactVersionId(artifactId: ArtifactId) = artifacts[artifactId]?.versionId
        ?: throw NotFoundException("Could not find artifact with id: $artifactId")

    suspend fun findArtifact(namespaceId: NamespaceId, schemaId: SchemaId, versionId: VersionId, filename: String) =
        artifacts.values
            .filter {
                it.namespaceId == namespaceId && it.schemaId == schemaId && it.versionId == versionId && it.artifact.filename == filename
            }
            .map { it.artifact }
            .singleOrNull()
            ?: throw NotFoundException("Could not find artifact: $filename in version with id: $versionId in schema with id: $schemaId and namespace with id: $namespaceId")

    suspend fun getOwner(artifactId: ArtifactId) =
        artifacts[artifactId]?.let { Triple(it.namespaceId, it.schemaId, it.versionId) }
            ?: throw NotFoundException("Could not find artifact with id: $artifactId")
}