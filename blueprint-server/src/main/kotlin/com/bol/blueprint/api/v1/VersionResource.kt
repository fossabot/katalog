package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.domain.toSemVerType
import com.bol.blueprint.queries.Query
import com.vdurmont.semver4j.Semver
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/v1/versions")
class VersionResource(
        private val handler: CommandHandler,
        private val query: Query
) {
    object Responses {
        data class Version(val id: UUID, val schemaId: UUID, val version: String)
        data class VersionCreated(val id: UUID)
    }

    object Requests {
        data class NewVersion(val schemaId: UUID, val version: String)
    }

    @GetMapping
    fun get(
            pagination: PaginationRequest?,
            @RequestParam schemaIds: List<UUID>?,
            @RequestParam latestPerMajorVersion: Boolean?,
            @RequestParam start: String?,
            @RequestParam stop: String?
    ): Page<Responses.Version> {
        var versions = getVersions(schemaIds)
                .sortedByDescending { it.version }

        if (latestPerMajorVersion != false) {
            versions = versions.distinctBy {
                val schema = query.getVersionSchemaOrThrow(it)
                Semver(it.version, schema.type.toSemVerType()).major
            }
        }

        if (start != null || stop != null) {
            versions = versions.filter { version ->
                val schema = query.getVersionSchemaOrThrow(version)
                val semStart = start?.let { Semver(it, schema.type.toSemVerType()) }
                val semStop = stop?.let { Semver(it, schema.type.toSemVerType()) }

                // Apply filter
                (semStart?.isLowerThanOrEqualTo(version.version) ?: true) && (semStop?.isGreaterThan(version.version)
                        ?: true)
            }
        }

        return versions
                .map {
                    Responses.Version(
                            id = it.id.id,
                            schemaId = query.getVersionSchemaOrThrow(it).id.id,
                            version = it.version
                    )
                }
                .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: UUID) =
            query.getVersion(VersionKey(id))?.let {
                val schema = query.getVersionSchemaOrThrow(it)
                Responses.Version(id = id, schemaId = schema.id.id, version = it.version)
            } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody data: Requests.NewVersion): Responses.VersionCreated {
        val key = VersionKey(id = UUID.randomUUID())
        val schemaKey = SchemaKey(data.schemaId)

        if (query.getVersions(listOf(schemaKey)).any { it.version == data.version }) throw ResponseStatusException(HttpStatus.CONFLICT)

        handler.createVersion(schemaKey, key, data.version)
        return Responses.VersionCreated(key.id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: UUID) {
        val key = VersionKey(id)

        query.getVersion(key)?.let {
            handler.deleteVersion(key)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    private fun getVersions(schemaIds: List<UUID>?) =
            schemaIds?.let {
                query.getVersions(schemaIds.map { id -> SchemaKey(id) })
            } ?: query.getVersions()
}