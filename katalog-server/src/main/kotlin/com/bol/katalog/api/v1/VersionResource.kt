package com.bol.katalog.api.v1

import com.bol.katalog.api.PaginationRequest
import com.bol.katalog.api.SortingRequest
import com.bol.katalog.api.paginate
import com.bol.katalog.api.sort
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.read
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
class VersionResource(private val registry: Aggregate<Registry>) {
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
        var result = registry.read {
            if (onlyCurrentVersions != false) {
                versions.getCurrentMajorVersions(schemaId)
            } else {
                versions.getAll(schemaId)
            }
        }

        result = result.sort(sorting) { column ->
            when (column) {
                "version" -> {
                    { it.toSemVer() }
                }
                "createdOn" -> {
                    { it.createdOn }
                }
                else -> {
                    { it.toSemVer() }
                }
            }
        }

        if (start != null || stop != null) {
            result = result.filter { version ->
                val semStart = start?.let { Semver(it, version.schema.type.toSemVerType()) }
                val semStop = stop?.let { Semver(it, version.schema.type.toSemVerType()) }

                // Apply filter
                (semStart?.isLowerThanOrEqualTo(version.toSemVer())
                    ?: true) && (semStop?.isGreaterThan(version.toSemVer())
                    ?: true)
            }
        }


        result
            .paginate(pagination) {
                toResponse(it)
            }
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: VersionId
    ) = monoWithUserId {
        toResponse(registry.read { versions.getById(id) })
    }

    @GetMapping("/find/{namespace}/{schema}/{version}")
    fun findOne(
        @PathVariable namespace: String,
        @PathVariable schema: String,
        @PathVariable version: String
    ) = monoWithUserId {
        val v = registry.read {
            val ns = namespaces.getByName(namespace)
            val s = schemas.getByName(ns.id, schema)
            versions.getByName(s.id, version)
        }

        toResponse(v)
    }

    private suspend fun toResponse(version: Version): Responses.Version {
        return registry.read {
            val semVer = version.toSemVer()
            Responses.Version(
                id = version.id,
                createdOn = version.createdOn,
                schemaId = version.schema.id,
                version = semVer.value,
                major = semVer.major,
                stable = semVer.isStable,
                current = versions.isCurrent(version)
            )
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody data: Requests.NewVersion
    ) = monoWithUserId {
        val id: VersionId = UUID.randomUUID().toString()
        registry.send(CreateVersionCommand(data.schemaId, id, data.version))
        Responses.VersionCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: VersionId
    ) = monoWithUserId {
        registry.send(DeleteVersionCommand(id))
    }
}
