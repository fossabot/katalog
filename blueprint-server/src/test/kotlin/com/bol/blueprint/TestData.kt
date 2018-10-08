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
    this.reset()
    createNamespace(TestData.NS1, GroupKey("group1"))
    createNamespace(TestData.NS2, GroupKey("group1"))
    createSchema(TestData.SCHEMA1, SchemaType.default())
    createSchema(TestData.SCHEMA2, SchemaType.default())
    createVersion(TestData.VERSION1)
    createVersion(TestData.VERSION2)
    createArtifact(TestData.ARTIFACT1, MediaType.JSON, byteArrayOf(1, 2, 3))
    createArtifact(TestData.ARTIFACT2, MediaType.JSON, byteArrayOf(1, 2, 3))
}
