package com.bol.blueprint.domain

import com.vdurmont.semver4j.Semver
import java.net.URI
import java.time.Instant
import java.util.*

data class Namespace(val id: NamespaceId, val name: String, val owner: GroupId)
data class Schema(val id: SchemaId, val name: String, val type: SchemaType)

data class SchemaType(val versioningScheme: VersioningScheme) {
    companion object {
        fun default() =
            SchemaType(versioningScheme = VersioningScheme.Semantic)
    }
}

enum class VersioningScheme {
    Semantic,
    Maven
}

data class Version(val id: VersionId, val createdOn: Instant, val semVer: Semver)

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

data class Artifact(val id: ArtifactId, val filename: String, val mediaType: MediaType)

typealias NamespaceId = UUID
typealias SchemaId = UUID
typealias VersionId = UUID
typealias ArtifactId = UUID

fun ArtifactId.getBlobStorePath(): URI = URI.create(this.toString())