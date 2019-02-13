package com.bol.katalog.api.v1

import com.bol.katalog.api.PaginationRequest
import com.bol.katalog.api.SortingRequest
import com.bol.katalog.api.paginate
import com.bol.katalog.api.sort
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.send
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.monoWithUserId
import com.vdurmont.semver4j.Semver
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/versions")
@PreAuthorize("hasRole('USER')")
class VersionResource(private val context: AggregateContext, private val registry: RegistryAggregate) {

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
        @RequestParam schemaId: SchemaId,
        @RequestParam onlyCurrentVersions: Boolean?,
        @RequestParam start: String?,
        @RequestParam stop: String?
    ) = monoWithUserId {
        val schema = registry.schemas.getById(schemaId)

        var result = if (onlyCurrentVersions != false) {
            registry.versions.getCurrentMajorVersions(schemaId)
        } else {
            registry.versions.getAll(schemaId)
        }

        result = result.sort(sorting) { column ->
            when (column) {
                "version" -> {
                    { it.toSemVer(schema) }
                }
                "createdOn" -> {
                    { it.createdOn }
                }
                else -> {
                    { it.toSemVer(schema) }
                }
            }
        }

        if (start != null || stop != null) {
            result = result.filter { version ->
                val semStart = start?.let { Semver(it, schema.type.toSemVerType()) }
                val semStop = stop?.let { Semver(it, schema.type.toSemVerType()) }

                // Apply filter
                (semStart?.isLowerThanOrEqualTo(version.toSemVer(schema))
                    ?: true) && (semStop?.isGreaterThan(version.toSemVer(schema))
                    ?: true)
            }
        }


        result
            .paginate(pagination) {
                toResponse(it, schema)
            }
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: VersionId
    ) = monoWithUserId {
        val version = registry.versions.getById(id)
        val schema = registry.schemas.getById(version.schemaId)
        toResponse(version, schema)
    }

    @GetMapping("/find/{namespace}/{schema}/{version}")
    fun findOne(
        @PathVariable namespace: String,
        @PathVariable schema: String,
        @PathVariable version: String
    ) = monoWithUserId {
        val ns = registry.namespaces.getByName(namespace)
        val s = registry.schemas.getByName(ns.id, schema)
        val v = registry.versions.getByVersion(s.id, version)

        toResponse(v, s)
    }

    private suspend fun toResponse(version: Version, schema: Schema): Responses.Version {
        val semVer = version.toSemVer(schema)
        return Responses.Version(
            id = version.id,
            createdOn = version.createdOn,
            schemaId = version.schemaId,
            version = semVer.value,
            major = semVer.major,
            stable = semVer.isStable,
            current = registry.versions.isCurrent(version)
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody data: Requests.NewVersion
    ) = monoWithUserId {
        val id: VersionId = UUID.randomUUID().toString()
        context.send(CreateVersionCommand(data.schemaId, id, data.version))
        Responses.VersionCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: VersionId
    ) = monoWithUserId {
        context.send(DeleteVersionCommand(id))
    }
}