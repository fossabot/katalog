package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.*
import com.bol.blueprint.domain.readmodels.ArtifactReadModel
import com.bol.blueprint.domain.readmodels.NamespaceReadModel
import com.bol.blueprint.domain.readmodels.SchemaReadModel
import com.bol.blueprint.domain.readmodels.VersionReadModel
import com.bol.blueprint.store.BlobStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RestController
@RequestMapping("/api/v1/repository/{namespace}/{schema}/{version}")
class RepositoryResource(
    private val namespaces: NamespaceReadModel,
    private val schemas: SchemaReadModel,
    private val versions: VersionReadModel,
    private val artifacts: ArtifactReadModel,
    private val blobStore: BlobStore
) {
    @GetMapping("/{filename}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String) =
        GlobalScope.mono {
            namespaces.findNamespace(namespace)?.let { ns ->
                schemas.findSchema(ns.id, schema)?.let { s ->
                    versions.findVersion(ns.id, s.id, version)?.let { v ->
                        val artifact = artifacts.findArtifact(ns.id, s.id, v.id, filename)
                        artifact?.id?.getBlobStorePath()?.let { path ->
                            blobStore.get(path)?.let {
                                it
                            }
                        }

                    }
                }
            } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
}

fun Artifact.getRepositoryPath(namespace: Namespace, schema: Schema, version: Version): URI {
    return URI.create("/api/v1/repository/${namespace.name}/${schema.name}/${version.semVer.value}/$filename")
}