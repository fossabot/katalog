package com.bol.blueprint.domain

import org.springframework.http.MediaType
import java.net.URI

data class Namespace(val name: String)
data class Schema(val name: String, val type: SchemaType)

data class SchemaType(val versioningScheme: VersioningScheme) {
    companion object {
        fun default() = SchemaType(versioningScheme = VersioningScheme.Semantic)
    }
}

enum class VersioningScheme {
    Semantic,
    Maven
}

data class Version(val version: String)

data class Artifact(val filename: String, val mediaType: MediaType, val path: URI)

data class NamespaceKey(val namespace: String)
data class SchemaKey(val namespace: String, val schema: String)
data class VersionKey(val namespace: String, val schema: String, val version: String)
data class ArtifactKey(val namespace: String, val schema: String, val version: String, val filename: String)
