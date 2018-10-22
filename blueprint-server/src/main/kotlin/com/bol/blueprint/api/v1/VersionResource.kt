package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.queries.Query
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    fun get(pagination: PaginationRequest?, @RequestParam schemaIds: List<UUID>?): Page<Responses.Version> {
        val versions = schemaIds?.let {
            query.getVersions(schemaIds.map { id -> SchemaKey(id) })
        } ?: query.getVersions()

        return versions
                .map {
                    Responses.Version(
                            id = it.key.id,
                            schemaId = getSchemaOrThrow(it.key),
                            version = it.value.version
                    )
                }
                .sortedBy { it.version }
                .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: UUID) =
            query.getVersion(VersionKey(id))?.let {
                ResponseEntity.ok(Responses.Version(id = id, schemaId = getSchemaOrThrow(VersionKey(id)), version = it.version))
            } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody data: Requests.NewVersion): Responses.VersionCreated {
        val key = VersionKey(id = UUID.randomUUID())
        val schemaKey = SchemaKey(data.schemaId)

        if (query.getVersions(listOf(schemaKey)).values.any { it.version == data.version }) throw ResponseStatusException(HttpStatus.CONFLICT)

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

    fun getSchemaOrThrow(id: VersionKey) = query.getVersionSchema(id)?.id
            ?: throw RuntimeException("Could not find the schema belonging to version: $id")
}