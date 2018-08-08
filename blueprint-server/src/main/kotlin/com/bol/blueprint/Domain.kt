package com.bol.blueprint

data class Namespace(val name: String)
data class Schema(val name: String, val type: SchemaType)

sealed class SchemaType {
    abstract val versioningScheme: VersioningScheme

    companion object {
        fun default() = Swagger2SchemaType(versioningScheme = MavenVersioningScheme(false))
    }
}

data class Swagger2SchemaType(override val versioningScheme: VersioningScheme) : SchemaType()
data class JsonSchemaType(override val versioningScheme: VersioningScheme) : SchemaType()

sealed class VersioningScheme {
    abstract val dummy: Boolean
}

data class MavenVersioningScheme(override val dummy: Boolean) : VersioningScheme()
data class SemanticVersioningScheme(override val dummy: Boolean) : VersioningScheme()

data class Version(val version: String)

data class Artifact(val filename: String)

data class NamespaceKey(val namespace: String)
data class SchemaKey(val namespace: String, val schema: String)
data class VersionKey(val namespace: String, val schema: String, val version: String)
