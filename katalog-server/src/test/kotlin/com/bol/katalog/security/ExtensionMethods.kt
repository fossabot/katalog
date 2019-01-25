package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission

/**
 * These extension methods can be used to simplify the creation of commands/events in testing
 */
fun Group.create() = CreateGroupCommand(id, name)

fun Group.created() = GroupCreatedEvent(id, name)
fun Group.disabled() = GroupDisabledEvent(id)
fun User.create() = CreateUserCommand(id, username, encodedPassword, authorities)
fun User.created() = UserCreatedEvent(id, username, encodedPassword, authorities)
fun User.addToGroup(group: Group, permissions: Set<GroupPermission>) = AddUserToGroupCommand(id, group.id, permissions)
fun User.addedToGroup(group: Group, permissions: Set<GroupPermission>) =
    UserAddedToGroupEvent(id, group.id, permissions)

fun User.removeFromGroup(group: Group) = RemoveUserFromGroupCommand(id, group.id)
fun User.removedFromGroup(group: Group) = UserRemovedFromGroupEvent(id, group.id)
