package com.bol.katalog.api.v1

import com.bol.katalog.domain.Processor
import com.bol.katalog.domain.SchemaId
import com.bol.katalog.domain.Version
import com.bol.katalog.domain.VersionId
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.domain.aggregates.SchemaAggregate
import com.bol.katalog.domain.aggregates.VersionAggregate
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/versions")
@PreAuthorize("hasRole('USER')")
class VersionResource(
    private val handler: Processor,
    private val namespaces: NamespaceAggregate,
    private val schemas: SchemaAggregate,
    private val versions: VersionAggregate
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
        pagination: PaginationRequest,
        sorting: SortingRequest,
        @RequestParam schemaIds: List<SchemaId>?,
        @RequestParam onlyCurrentVersions: Boolean?,
        @RequestParam start: String?,
        @RequestParam stop: String?
    ) = GlobalScope.mono {
        val filtered = (schemaIds ?: schemas.getSchemas().map { it.id }).flatMap { schemaId ->
            var result: Collection<Version> = versions.getVersions(schemaId)

            result = result.sort(sorting) { column ->
                when (column) {
                    "version" -> {
                        { it.semVer }
                    }
                    "createdOn" -> {
                        { it.createdOn }
                    }
                    else -> {
                        { it.semVer }
                    }
                }
            }

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

        filtered
            .paginate(pagination) {
                toResponse(it)
            }
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: VersionId) = GlobalScope.mono {
        toResponse(versions.getVersion(id))
    }

    @GetMapping("/find/{namespace}/{schema}/{version}")
    fun findOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String) =
        GlobalScope.mono {
        val ns = namespaces.findNamespace(namespace)
        val s = schemas.findSchema(ns.id, schema)
        val v = versions.findVersion(ns.id, s.id, version)
            toResponse(v)
    }

    private fun toResponse(version: Version): Responses.Version {
        val schemaId = versions.getVersionSchemaId(version.id)

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
        handler.createVersion(data.schemaId, id, data.version)
        Responses.VersionCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: VersionId) = GlobalScope.mono {
        handler.deleteVersion(id)
    }
}
