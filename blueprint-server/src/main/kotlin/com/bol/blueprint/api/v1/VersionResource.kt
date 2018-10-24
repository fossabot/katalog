package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.SchemaId
import com.bol.blueprint.domain.VersionId
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
        data class Version(val id: VersionId, val schemaId: SchemaId, val version: String)
        data class VersionCreated(val id: VersionId)
    }

    object Requests {
        data class NewVersion(val schemaId: SchemaId, val version: String)
    }

    @GetMapping
    fun get(
            pagination: PaginationRequest?,
            @RequestParam schemaIds: List<SchemaId>?,
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
                            id = it.id,
                            schemaId = query.getVersionSchemaOrThrow(it).id,
                            version = it.version
                    )
                }
                .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: VersionId) =
            query.getVersion(id)?.let {
                val schema = query.getVersionSchemaOrThrow(it)
                Responses.Version(id = id, schemaId = schema.id, version = it.version)
            } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody data: Requests.NewVersion): Responses.VersionCreated {
        val id: VersionId = UUID.randomUUID()

        if (query.getVersions(listOf(data.schemaId)).any { it.version == data.version }) throw ResponseStatusException(HttpStatus.CONFLICT)

        handler.createVersion(data.schemaId, id, data.version)
        return Responses.VersionCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: VersionId) {
        query.getVersion(id)?.let {
            handler.deleteVersion(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    private fun getVersions(schemaIds: List<SchemaId>?) =
            schemaIds?.let {
                query.getVersions(schemaIds)
            } ?: query.getVersions()
}