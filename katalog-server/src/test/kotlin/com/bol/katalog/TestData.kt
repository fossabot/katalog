package com.bol.katalog

import com.bol.katalog.TestData.artifact1
import com.bol.katalog.TestData.artifact2
import com.bol.katalog.TestData.ns1
import com.bol.katalog.TestData.ns1_schema1
import com.bol.katalog.TestData.ns1_schema1_v100
import com.bol.katalog.TestData.ns1_schema1_v101
import com.bol.katalog.TestData.ns1_schema1_v200snapshot
import com.bol.katalog.TestData.ns1_schema2
import com.bol.katalog.TestData.ns2
import com.bol.katalog.TestData.ns2_schema3
import com.bol.katalog.TestData.ns2_schema3_v100
import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.*
import com.bol.katalog.users.GroupPermission
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*

object TestData {
    val clock: Clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault())

    val ns1: NamespaceId = UUID.randomUUID()
    val ns2: NamespaceId = UUID.randomUUID()
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

suspend fun applyBasicTestSet(
    processor: CommandProcessor
) {
    with(processor) {
        apply(CreateGroupCommand("id-group1", "group1"))
        apply(CreateGroupCommand("id-group2", "group2"))
        apply(CreateGroupCommand("id-group3", "group3"))

        TestUsers.allUsers().forEach { apply(CreateUserCommand(it.id, it.username, "password", it.authorities)) }

        apply(AddUserToGroupCommand(TestUsers.user1().id, "id-group1", allPermissions()))
        apply(AddUserToGroupCommand(TestUsers.user1().id, "id-group2", setOf(GroupPermission.READ)))

        apply(AddUserToGroupCommand(TestUsers.user2().id, "id-group2", allPermissions()))
        apply(AddUserToGroupCommand(TestUsers.user2().id, "id-group3", setOf(GroupPermission.READ)))

        withTestUser1 {
            apply(CreateNamespaceCommand(ns1, "id-group1", "ns1"))
            apply(CreateNamespaceCommand(ns2, "id-group1", "ns2"))

            apply(CreateSchemaCommand(ns1, ns1_schema1, "schema1", SchemaType.default()))
            apply(CreateSchemaCommand(ns1, ns1_schema2, "schema2", SchemaType.default()))
            apply(CreateSchemaCommand(ns2, ns2_schema3, "schema3", SchemaType.default()))

            apply(CreateVersionCommand(ns1_schema1, ns1_schema1_v100, "1.0.0"))
            apply(CreateVersionCommand(ns1_schema1, ns1_schema1_v101, "1.0.1"))
            apply(CreateVersionCommand(ns1_schema1, ns1_schema1_v200snapshot, "2.0.0-SNAPSHOT"))

            apply(CreateVersionCommand(ns2_schema3, ns2_schema3_v100, "1.0.0"))

            apply(
                CreateArtifactCommand(
                    ns1_schema1_v100,
                    artifact1,
                    "artifact1.json",
                    MediaType.JSON,
                    byteArrayOf(1, 2, 3)
                )
            )
            apply(
                CreateArtifactCommand(
                    ns1_schema1_v101,
                    artifact2,
                    "artifact2.json",
                    MediaType.JSON,
                    byteArrayOf(4, 5, 6)
                )
            )
        }
    }
}

// Can be applied to a CommandHandler directly, without requiring a full Spring Security context
object TestUsers {
    fun allUsers() = listOf(user1(), user2(), admin(), noGroupsUser())

    fun user1() = User(
        "id-user1",
        "user1",
        "password",
        setOf(SimpleGrantedAuthority("ROLE_USER"))
    )

    fun user2() = User(
        "id-user2",
        "user2",
        "password",
        setOf(SimpleGrantedAuthority("ROLE_USER"))
    )

    fun admin() = User(
        "id-admin",
        "admin",
        "password",
        setOf(
            SimpleGrantedAuthority("ROLE_USER"),
            SimpleGrantedAuthority("ROLE_ADMIN")
        )
    )

    fun noGroupsUser() = User(
        "id-no-groups-user",
        "no-groups-user",
        "password",
        setOf(
            SimpleGrantedAuthority("ROLE_USER")
        )
    )
}