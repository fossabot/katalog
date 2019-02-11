package com.bol.katalog.features.registry

import com.bol.katalog.security.GroupId
import com.bol.katalog.security.HasGroupId
import com.vdurmont.semver4j.Semver
import java.net.URI
import java.time.Instant

data class Namespace(val id: NamespaceId, val name: String, override val groupId: GroupId, val createdOn: Instant) :
    HasGroupId

data class Schema(
    val id: SchemaId,
    override val groupId: GroupId,
    val namespaceId: NamespaceId,
    val createdOn: Instant,
    val name: String,
    val type: SchemaType
) : HasGroupId

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

data class Version(
    val id: VersionId,
    override val groupId: GroupId,
    val schemaId: SchemaId,
    val createdOn: Instant,
    val version: String
) : HasGroupId

fun Version.toSemVer(schema: Schema): Semver = Semver(this.version, schema.type.toSemVerType())

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
    override val groupId: GroupId,
    val versionId: VersionId,
    val filename: String,
    val filesize: Int,
    val mediaType: MediaType
) : HasGroupId

typealias NamespaceId = String
typealias SchemaId = String
typealias VersionId = String
typealias ArtifactId = String

fun getBlobStorePath(artifactId: ArtifactId): URI = URI.create(artifactId)
