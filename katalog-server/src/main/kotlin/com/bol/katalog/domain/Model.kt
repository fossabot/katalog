package com.bol.katalog.domain

import com.vdurmont.semver4j.Semver
import java.net.URI
import java.time.Instant
import java.util.*

data class Namespace(val id: NamespaceId, val name: String, val group: Group, val createdOn: Instant)
data class Schema(val id: SchemaId, val createdOn: Instant, val name: String, val type: SchemaType)

data class SchemaType(val versioningScheme: VersioningScheme) {
    companion object {
        fun default() =
            SchemaType(versioningScheme = VersioningScheme.Semantic)
    }

    fun toSemVerType() =
        when (this.versioningScheme) {
            VersioningScheme.Semantic -> Semver.SemverType.NPM
            VersioningScheme.Maven -> Semver.SemverType.IVY
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

data class Artifact(val id: ArtifactId, val filename: String, val filesize: Int, val mediaType: MediaType)

typealias NamespaceId = UUID
typealias SchemaId = UUID
typealias VersionId = UUID
typealias ArtifactId = UUID

fun ArtifactId.getBlobStorePath(): URI = URI.create(this.toString())