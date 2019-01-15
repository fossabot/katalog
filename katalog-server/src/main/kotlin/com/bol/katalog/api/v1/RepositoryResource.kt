package com.bol.katalog.api.v1

import com.bol.katalog.features.registry.*
import com.bol.katalog.features.registry.aggregates.ArtifactAggregate
import com.bol.katalog.features.registry.aggregates.NamespaceAggregate
import com.bol.katalog.features.registry.aggregates.SchemaAggregate
import com.bol.katalog.features.registry.aggregates.VersionAggregate
import com.bol.katalog.store.BlobStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/repository/{namespace}/{schema}/{version}")
class RepositoryResource(
    private val namespaces: NamespaceAggregate,
    private val schemas: SchemaAggregate,
    private val versions: VersionAggregate,
    private val artifacts: ArtifactAggregate,
    private val blobStore: BlobStore
) {
    @GetMapping("/{filename}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String) =
        GlobalScope.mono {
            val ns = namespaces.findUnauthorizedNamespace(namespace)
            val s = schemas.findSchema(ns.id, schema)
            val v = versions.findVersion(ns.id, s.id, version)

            val artifact = artifacts.findArtifact(ns.id, s.id, v.id, filename)
            val path = getBlobStorePath(artifact.id)

            blobStore.get(path)?.let { it }
        }
}

fun Artifact.getRepositoryPath(namespace: Namespace, schema: Schema, version: Version): URI {
    return URI.create("/api/v1/repository/${namespace.name}/${schema.name}/${version.semVer.value}/$filename")
}