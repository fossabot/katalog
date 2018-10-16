package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.domain.Schema
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.Version
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.reactor.mono
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/browse")
@Validated
class BrowseResource(
        private val query: Query
) {
    object Responses {
        data class BrowseNamespace(
                val name: String,
                val schemas: List<BrowseSchema>
        )

        data class BrowseSchema(
                val name: String,
                val versions: List<BrowseVersion>
        )

        data class BrowseVersion(
                val version: String
        )
    }

    @GetMapping
    fun get(pagination: PaginationRequest?) = GlobalScope.mono {
        query
                .getNamespaces()
                .sortedBy { it.name }
                .paginate(pagination, 25) {
                    Responses.BrowseNamespace(
                            name = it.name,
                            schemas = mapToBrowseSchemas(it.name, query.getSchemas(NamespaceKey(it.name)))
                    )
                }
    }

    private fun mapToBrowseSchemas(namespace: String, schemas: Sequence<Schema>): List<Responses.BrowseSchema> {
        return schemas
                .map { schema ->
                    Responses.BrowseSchema(
                            name = schema.name,
                            versions = mapToBrowseVersions(query.getVersions(SchemaKey(namespace, schema.name)))
                    )
                }
                .sortedBy { it.name }
                .toList()
    }

    private fun mapToBrowseVersions(versions: Sequence<Version>): List<Responses.BrowseVersion> {
        return versions
                .map { version ->
                    Responses.BrowseVersion(
                            version = version.version
                    )
                }
                .sortedByDescending { it.version }
                .toList()
    }
}