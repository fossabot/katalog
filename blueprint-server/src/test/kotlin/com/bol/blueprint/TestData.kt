package com.bol.blueprint

import com.bol.blueprint.TestData.artifact1
import com.bol.blueprint.TestData.artifact2
import com.bol.blueprint.TestData.group1
import com.bol.blueprint.TestData.ns1
import com.bol.blueprint.TestData.ns1_schema1
import com.bol.blueprint.TestData.ns1_schema1_v100
import com.bol.blueprint.TestData.ns1_schema1_v101
import com.bol.blueprint.TestData.ns1_schema1_v200snapshot
import com.bol.blueprint.TestData.ns1_schema2
import com.bol.blueprint.TestData.ns2
import com.bol.blueprint.TestData.ns2_schema3
import com.bol.blueprint.TestData.ns2_schema3_v100
import com.bol.blueprint.domain.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*

object TestData {
    val clock: Clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault())

    val ns1: NamespaceId = UUID.randomUUID()
    val ns2: NamespaceId = UUID.randomUUID()
    val group1: GroupId = UUID.randomUUID()
    val ns1_schema1: SchemaId = UUID.randomUUID()
    val ns1_schema2: SchemaId = UUID.randomUUID()
    val ns2_schema3: SchemaId = UUID.randomUUID()
    val ns1_schema1_v100: VersionId = UUID.randomUUID()
    val ns1_schema1_v101: VersionId = UUID.randomUUID()
    val ns1_schema1_v200snapshot: VersionId = UUID.randomUUID()
    val ns2_schema3_v100: VersionId = UUID.randomUUID()
    val artifact1: ArtifactId = UUID.randomUUID()
    val artifact2: ArtifactId = UUID.randomUUID()
}

suspend fun CommandHandler.applyBasicTestSet() {
    this.reset()

    createNamespace(ns1, group1, "ns1")
    createNamespace(ns2, group1, "ns2")

    createSchema(ns1, ns1_schema1, "schema1", SchemaType.default())
    createSchema(ns1, ns1_schema2, "schema2", SchemaType.default())
    createSchema(ns2, ns2_schema3, "schema3", SchemaType.default())

    createVersion(ns1_schema1, ns1_schema1_v100, "1.0.0")
    createVersion(ns1_schema1, ns1_schema1_v101, "1.0.1")
    createVersion(ns1_schema1, ns1_schema1_v200snapshot, "2.0.0-SNAPSHOT")

    createVersion(ns2_schema3, ns2_schema3_v100, "1.0.0")

    createArtifact(ns1_schema1_v100, artifact1, "artifact1.json", MediaType.JSON, byteArrayOf(1, 2, 3))
    createArtifact(ns1_schema1_v101, artifact2, "artifact2.json", MediaType.JSON, byteArrayOf(4, 5, 6))
}

// Can be applied to a CommandHandler directly, without requiring a full Spring Security context
object TestUsers {
    fun user() = object : CurrentUserSupplier {
        override suspend fun getCurrentUser() =
                BlueprintUserDetails(
                        "user",
                        "password",
                        listOf(SimpleGrantedAuthority("ROLE_USER")),
                        listOf(Group("group1"), Group("group2"))
                )
    }
}