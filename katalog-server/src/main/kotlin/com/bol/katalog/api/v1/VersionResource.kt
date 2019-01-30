package com.bol.katalog.api.v1

import com.bol.katalog.api.PaginationRequest
import com.bol.katalog.api.SortingRequest
import com.bol.katalog.api.paginate
import com.bol.katalog.api.sort
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.monoWithUserId
import com.bol.katalog.users.GroupPermission
import com.vdurmont.semver4j.Semver
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/versions")
@PreAuthorize("hasRole('USER')")
class VersionResource(
    private val registry: Aggregate<Registry>,
    private val permissionManager: PermissionManager
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
    ) = monoWithUserId {
        val filtered = (schemaIds ?: registry.read { getSchemas() }.map { it.id }).flatMap { schemaId ->
            var result: Collection<Version> = registry.read {
                if (onlyCurrentVersions != false) {
                    getCurrentMajorVersions(schemaId)
                } else {
                    getVersions(schemaId)
                }
            }

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

            if (start != null || stop != null) {
                result = result.filter { version ->
                    val semStart = start?.let { Semver(it, version.semVer.type) }
                    val semStop = stop?.let { Semver(it, version.semVer.type) }

                    // Apply filter
                    (semStart?.isLowerThanOrEqualTo(version.semVer)
                        ?: true) && (semStop?.isGreaterThan(version.semVer)
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
    fun getOne(
        @PathVariable id: VersionId
    ) = monoWithUserId {
        toResponse(registry.read { getVersion(id) })
    }

    @GetMapping("/find/{namespace}/{schema}/{version}")
    fun findOne(
        @PathVariable namespace: String,
        @PathVariable schema: String,
        @PathVariable version: String
    ) = monoWithUserId {
        val v = registry.read {
            val ns = namespaces.getByName(namespace)
            val s = findSchema(ns.id, schema)
            findVersion(ns.id, s.id, version)
        }

        toResponse(v)
    }

    private suspend fun toResponse(version: Version): Responses.Version {
        return registry.read {
            Responses.Version(
                id = version.id,
                createdOn = version.createdOn,
                schemaId = version.schema.id,
                version = version.semVer.value,
                major = version.semVer.major,
                stable = version.semVer.isStable,
                current = isCurrent(version)
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
        val version = registry.read { getVersion(id) }
        permissionManager.requirePermission(version.schema.namespace.groupId, GroupPermission.DELETE) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        registry.send(DeleteVersionCommand(id))
    }
}
