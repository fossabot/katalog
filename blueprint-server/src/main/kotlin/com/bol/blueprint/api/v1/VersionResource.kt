package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.*
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/versions")
class VersionResource(
    private val handler: CommandHandler,
    private val query: Query
) {
    object Responses {
        data class Version(
            val id: VersionId,
            val createdOn: Instant,
            val schemaId: SchemaId,
            val version: String,
            val major: Int,
            val stable: Boolean,
            val current: Boolean
        )

        data class VersionCreated(val id: VersionId)
    }

    object Requests {
        data class NewVersion(val schemaId: SchemaId, val version: String)
    }

    @GetMapping
    fun get(
        pagination: PaginationRequest?,
        @RequestParam schemaIds: List<SchemaId>?,
        @RequestParam onlyCurrentVersions: Boolean?,
        @RequestParam start: String?,
        @RequestParam stop: String?
    ): Page<Responses.Version> {
        val filtered = (schemaIds ?: query.getSchemas().map { it.id }).flatMap { schemaId ->
            var versions: Collection<Version> = query.getVersions(schemaId)
                .sortedByDescending { it.semVer }

            if (onlyCurrentVersions != false) {
                versions = query.getCurrentMajorVersions(versions)
            }

            if (start != null || stop != null) {
                versions = versions.filter { version ->
                    val semStart = start?.let { Semver(it, version.semVer.type) }
                    val semStop = stop?.let { Semver(it, version.semVer.type) }

                    // Apply filter
                    (semStart?.isLowerThanOrEqualTo(version.semVer) ?: true) && (semStop?.isGreaterThan(version.semVer)
                        ?: true)
                }
            }

            versions
        }

        return filtered
            .map { toResponse(it) }
            .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: VersionId) =
        query.getVersion(id)?.let { toResponse(it) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    @GetMapping("/find/{namespace}/{schema}/{version}")
    fun findOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String) =
        query.findVersion(namespace, schema, version)?.let { toResponse(it) } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND
        )

    private fun toResponse(version: Version): Responses.Version {
        val schema = query.getVersionSchemaOrThrow(version)

        return Responses.Version(
            id = version.id,
            createdOn = version.createdOn,
            schemaId = schema.id,
            version = version.semVer.value,
            major = version.semVer.major,
            stable = version.semVer.isStable,
            current = query.isCurrent(schema, version)
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody data: Requests.NewVersion) = GlobalScope.mono {
        val id: VersionId = UUID.randomUUID()

        if (query.getVersions(data.schemaId).any { it.semVer.value == data.version }) throw ResponseStatusException(
            HttpStatus.CONFLICT
        )

        handler.createVersion(data.schemaId, id, data.version)
        Responses.VersionCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: VersionId) = GlobalScope.mono {
        query.getVersion(id)?.let {
            handler.deleteVersion(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}
