package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.Handler
import com.bol.blueprint.domain.SchemaId
import com.bol.blueprint.domain.Version
import com.bol.blueprint.domain.VersionId
import com.bol.blueprint.domain.readmodels.NamespaceReadModel
import com.bol.blueprint.domain.readmodels.SchemaReadModel
import com.bol.blueprint.domain.readmodels.VersionReadModel
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
    private val handler: Handler,
    private val namespaces: NamespaceReadModel,
    private val schemas: SchemaReadModel,
    private val versions: VersionReadModel
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
        val filtered = (schemaIds ?: schemas.getSchemas().map { it.id }).flatMap { schemaId ->
            var result: Collection<Version> = versions.getVersions(schemaId)
                .sortedByDescending { it.semVer }

            if (onlyCurrentVersions != false) {
                result = versions.getCurrentMajorVersions(result)
            }

            if (start != null || stop != null) {
                result = result.filter { version ->
                    val semStart = start?.let { Semver(it, version.semVer.type) }
                    val semStop = stop?.let { Semver(it, version.semVer.type) }

                    // Apply filter
                    (semStart?.isLowerThanOrEqualTo(version.semVer) ?: true) && (semStop?.isGreaterThan(version.semVer)
                        ?: true)
                }
            }

            result
        }

        return filtered
            .map { toResponse(it) }
            .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: VersionId) =
        versions.getVersion(id)?.let { toResponse(it) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    @GetMapping("/find/{namespace}/{schema}/{version}")
    fun findOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String): Responses.Version {
        return namespaces.findNamespace(namespace)?.let { ns ->
            schemas.findSchema(ns.id, schema)?.let { s ->
                versions.findVersion(ns.id, s.id, version)?.let { toResponse(it) }
            }
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    private fun toResponse(version: Version): Responses.Version {
        val schemaId = versions.getVersionSchemaId(version.id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return Responses.Version(
            id = version.id,
            createdOn = version.createdOn,
            schemaId = schemaId,
            version = version.semVer.value,
            major = version.semVer.major,
            stable = version.semVer.isStable,
            current = versions.isCurrent(schemaId, version)
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody data: Requests.NewVersion) = GlobalScope.mono {
        val id: VersionId = UUID.randomUUID()

        if (versions.getVersions(data.schemaId).any { it.semVer.value == data.version }) throw ResponseStatusException(
            HttpStatus.CONFLICT
        )

        handler.createVersion(data.schemaId, id, data.version)
        Responses.VersionCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: VersionId) = GlobalScope.mono {
        versions.getVersion(id)?.let {
            handler.deleteVersion(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}
