package com.bol.katalog.api.v1

import com.bol.katalog.features.registry.Artifact
import com.bol.katalog.features.registry.RegistryAggregate
import com.bol.katalog.features.registry.getBlobStorePath
import com.bol.katalog.security.SystemUser
import com.bol.katalog.security.withUserId
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
    private val registry: RegistryAggregate,
    private val blobStore: BlobStore
) {
    @GetMapping("/{filename}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String) =
        GlobalScope.mono {
            withUserId(SystemUser.get().id) {
                val ns = registry.namespaces.getByName(namespace)
                val s = registry.schemas.getByName(ns.id, schema)
                val v = registry.versions.getByVersion(s.id, version)
                val artifact = registry.artifacts.getByName(v.id, filename)
                val path = getBlobStorePath(artifact.id)

                blobStore.get(path)?.let { it }
            }
        }
}

suspend fun Artifact.getRepositoryPath(registry: RegistryAggregate): URI {
    val v = registry.versions.getById(this.versionId)
    val s = registry.schemas.getById(v.schemaId)
    val n = registry.namespaces.getById(s.namespaceId)
    return URI.create("/api/v1/repository/${n.name}/${s.name}/${v.version}/$filename")
}