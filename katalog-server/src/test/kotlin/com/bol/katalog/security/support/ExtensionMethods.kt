package com.bol.katalog.security.support

import com.bol.katalog.features.registry.NamespaceId
import com.bol.katalog.security.*
import com.bol.katalog.security.tokens.*
import com.bol.katalog.users.GroupPermission
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * Define some default groups and users
 * These are added to the security system in TestApplication, where the testSecurityAggregate is defined
 */
val group1 = Group(
    GroupId("id-group1"), "group1", emptyList()
)
val group2 = Group(
    GroupId("id-group2"), "group2", emptyList()
)
val user1 = User("id-user1", "user1", "password", setOf(SimpleGrantedAuthority("ROLE_USER")))
val user2 = User("id-user2", "user2", "password", setOf(SimpleGrantedAuthority("ROLE_USER")))
val userReadOnly = User(
    "id-user-read-only",
    "user-read-only",
    "password",
    setOf(SimpleGrantedAuthority("ROLE_USER"))
)
val admin = User(
    "id-admin", "admin", "password", setOf(
        SimpleGrantedAuthority("ROLE_USER"),
        SimpleGrantedAuthority("ROLE_ADMIN")
    )
)
val userNoGroups = User(
    "id-user-no-groups",
    "user-no-groups",
    "password",
    setOf(SimpleGrantedAuthority("ROLE_USER"))
)

/**
 * These extension methods can be used to simplify the creation of commands/events in testing
 */
fun Group.create() = CreateGroupCommand(id, name)

fun Group.created() = GroupCreatedEvent(id, name)
fun Group.disable() = DisableGroupCommand(id)
fun Group.disabled() = GroupDisabledEvent(id)
fun User.create() =
    CreateUserCommand(id, username, encodedPassword, authorities.map { it.authority }.toSet())

fun User.created() =
    UserCreatedEvent(id, username, encodedPassword, authorities.map { it.authority }.toSet())
fun User.addToGroup(group: Group, permissions: Set<GroupPermission> = emptySet()) =
    AddUserToGroupCommand(id, group.id, permissions)

fun User.addedToGroup(group: Group, permissions: Set<GroupPermission> = emptySet()) =
    UserAddedToGroupEvent(id, group.id, permissions)

fun User.removeFromGroup(group: Group) =
    RemoveUserFromGroupCommand(id, group.id)

fun User.removedFromGroup(group: Group) =
    UserRemovedFromGroupEvent(id, group.id)
fun User.disable() = DisableUserCommand(id)
fun User.disabled() = UserDisabledEvent(id)

fun Token.issue(namespaceId: NamespaceId, permissions: Set<GroupPermission>) =
    IssueTokenCommand(id, description, subjectId, namespaceId, permissions)

fun Token.issued() = TokenIssuedEvent(id, description, subjectId, token)
fun Token.revoke() = RevokeTokenCommand(id)
fun Token.revoked() = TokenRevokedEvent(id)
