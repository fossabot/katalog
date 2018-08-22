package com.bol.blueprint

import com.bol.blueprint.domain.*

object TestData {
    val NS1 = NamespaceKey("ns1")
    val NS2 = NamespaceKey("ns2")
    val SCHEMA1 = SchemaKey("ns1", "schema1")
    val SCHEMA2 = SchemaKey("ns1", "schema2")
    val VERSION1 = VersionKey("ns1", "schema1", "1.0.0")
    val VERSION2 = VersionKey("ns1", "schema1", "1.0.1")
    val ARTIFACT1 = ArtifactKey("ns1", "schema1", "1.0.0", "artifact1.json")
    val ARTIFACT2 = ArtifactKey("ns1", "schema1", "1.0.0", "artifact2.json")
}

suspend fun CommandHandler.applyBasicTestSet() {
    applyTestSet(TestData.NS1, TestData.NS2, TestData.SCHEMA1, TestData.SCHEMA2, TestData.VERSION1, TestData.VERSION2, TestData.ARTIFACT1, TestData.ARTIFACT2)
}

suspend fun CommandHandler.applyTestSet(vararg items: Any) {
    this.reset()
    items.forEach {
        when (it) {
            is NamespaceKey -> createNamespace(it)
            is SchemaKey -> createSchema(it, SchemaType.default())
            is VersionKey -> createVersion(it)
            is ArtifactKey -> createArtifact(it, MediaType.JSON, byteArrayOf(1, 2, 3))
            else -> throw UnsupportedOperationException("Unknown key: $it")
        }
    }
}
