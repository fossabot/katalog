package com.bol.katalog

import com.bol.katalog.features.registry.*
import com.bol.katalog.security.*
import com.bol.katalog.users.GroupPermission
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

object TestData {
    val clock: Clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault())
}

suspend fun applyBasicUsersAndGroups(
    security: SecurityAggregate
) {
    with(security) {
        send(CreateGroupCommand("id-group1", "group1"))
        send(CreateGroupCommand("id-group2", "group2"))
        send(CreateGroupCommand("id-group3", "group3"))

        send(
            CreateUserCommand(
                "id-user1", "user1", "password", setOf(
                    SimpleGrantedAuthority("ROLE_USER")
                )
            )
        )
        send(
            CreateUserCommand(
                "id-user2", "user2", "password", setOf(
                    SimpleGrantedAuthority("ROLE_USER")
                )
            )
        )
        send(
            CreateUserCommand(
                "id-no-groups-user", "no-groups-user", "password", setOf(
                    SimpleGrantedAuthority("ROLE_USER")
                )
            )
        )
        send(
            CreateUserCommand(
                "id-admin",
                "admin",
                "password",
                setOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        )

        send(AddUserToGroupCommand("id-user1", "id-group1", allPermissions()))
        send(AddUserToGroupCommand("id-user1", "id-group2", setOf(GroupPermission.READ)))

        send(AddUserToGroupCommand("id-user2", "id-group2", allPermissions()))
        send(AddUserToGroupCommand("id-user2", "id-group3", setOf(GroupPermission.READ)))
    }
}

suspend fun applyBasicTestSet(
    registry: RegistryAggregate
) {
    with(registry) {
        send(CreateNamespaceCommand("id-ns1", "id-group1", "ns1"))
        send(CreateNamespaceCommand("id-ns2", "id-group1", "ns2"))

        send(CreateSchemaCommand("id-ns1", "id-ns1-schema1", "schema1", SchemaType.default()))
        send(CreateSchemaCommand("id-ns1", "id-ns1-schema2", "schema2", SchemaType.default()))
        send(CreateSchemaCommand("id-ns2", "id-ns2-schema3", "schema3", SchemaType.default()))

        send(CreateVersionCommand("id-ns1-schema1", "id-ns1-schema1-v100", "1.0.0"))
        send(CreateVersionCommand("id-ns1-schema1", "id-ns1-schema1-v101", "1.0.1"))
        send(CreateVersionCommand("id-ns1-schema1", "id-ns1-schema1-v200snapshot", "2.0.0-SNAPSHOT"))

        send(CreateVersionCommand("id-ns2-schema3", "id-ns2-schema3-v100", "1.0.0"))

        send(
            CreateArtifactCommand(
                "id-ns1-schema1-v100",
                "id-artifact1",
                "artifact1.json",
                MediaType.JSON,
                byteArrayOf(1, 2, 3)
            )
        )
        send(
            CreateArtifactCommand(
                "id-ns1-schema1-v101",
                "id-artifact2",
                "artifact2.json",
                MediaType.JSON,
                byteArrayOf(4, 5, 6)
            )
        )
    }
}