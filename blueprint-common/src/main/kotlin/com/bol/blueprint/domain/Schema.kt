package com.bol.blueprint.domain

import java.util.*

data class Namespace(val id: NamespaceKey, val name: String, val owner: GroupKey)
data class Schema(val id: SchemaKey, val name: String, val type: SchemaType)

data class SchemaType(val versioningScheme: VersioningScheme) {
    companion object {
        fun default() = SchemaType(versioningScheme = VersioningScheme.Semantic)
    }
}

enum class VersioningScheme {
    Semantic,
    Maven
}

data class Version(val id: VersionKey, val version: String)

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

data class Artifact(val id: ArtifactKey, val filename: String, val mediaType: MediaType)

data class NamespaceKey(val id: UUID)
data class SchemaKey(val id: UUID)
data class VersionKey(val id: UUID)
data class ArtifactKey(val id: UUID)
