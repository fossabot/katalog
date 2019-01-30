package com.bol.katalog.api.v1

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.features.registry.Artifact
import com.bol.katalog.features.registry.Registry
import com.bol.katalog.features.registry.getBlobStorePath
import com.bol.katalog.security.SystemUser
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
    private val registry: Aggregate<Registry>,
    private val blobStore: BlobStore
) {
    @GetMapping("/{filename}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String) =
        GlobalScope.mono {
            registry.readAs(SystemUser.get().id) {
                val ns = namespaces.getByName(namespace)
                val s = schemas.getByName(ns.id, schema)
                val v = findVersion(ns.id, s.id, version)
                val artifact = findArtifact(ns.id, s.id, v.id, filename)
                val path = getBlobStorePath(artifact.id)

                blobStore.get(path)?.let { it }
            }
        }
}

fun Artifact.getRepositoryPath(): URI {
    return URI.create("/api/v1/repository/${version.schema.namespace.name}/${version.schema.name}/${version.semVer.value}/$filename")
}