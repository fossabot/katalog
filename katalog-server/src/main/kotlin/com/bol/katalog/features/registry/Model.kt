package com.bol.katalog.features.registry

import com.bol.katalog.security.GroupId
import com.vdurmont.semver4j.Semver
import java.net.URI
import java.time.Instant

data class Namespace(val id: NamespaceId, val name: String, val groupId: GroupId, val createdOn: Instant)
data class Schema(
    val id: SchemaId,
    val createdOn: Instant,
    val name: String,
    val type: SchemaType,
    val namespace: Namespace
)

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

data class Version(val id: VersionId, val createdOn: Instant, val semVer: Semver, val schema: Schema)

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

data class Artifact(
    val id: ArtifactId,
    val filename: String,
    val filesize: Int,
    val mediaType: MediaType,
    val version: Version
)

typealias NamespaceId = String
typealias SchemaId = String
typealias VersionId = String
typealias ArtifactId = String

fun getBlobStorePath(artifactId: ArtifactId): URI = URI.create(artifactId)