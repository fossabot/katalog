package com.bol.blueprint.domain

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

data class NamespaceKey(val namespace: String)
data class SchemaKey(val namespace: String, val schema: String)
data class VersionKey(val namespace: String, val schema: String, val version: String)
data class ArtifactKey(val namespace: String, val schema: String, val version: String, val filename: String)
