package com.bol.blueprint.domain

import java.net.URI
import java.util.*

data class Namespace(val name: String, val owner: GroupKey)
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

enum class MediaType(val mime: String) {
    JSON("application/json"),
    XML("application/xml");

    companion object {
        fun fromFilename(filename: String) = when {
            filename.toLowerCase().endsWith(".json") -> JSON
            filename.toLowerCase().endsWith(".xml") -> XML
            else -> throw UnsupportedOperationException("Could not determine media type from filename: $filename")
        }
    }
}

data class Artifact(val filename: String, val mediaType: MediaType, val path: URI)

data class NamespaceKey(val id: UUID)
data class SchemaKey(val id: UUID)
data class VersionKey(val id: UUID)
data class ArtifactKey(val id: UUID)
