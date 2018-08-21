package com.bol.blueprint

import com.bol.blueprint.domain.ArtifactKey
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.VersionKey

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
