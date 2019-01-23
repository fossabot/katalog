package com.bol.katalog.security

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.State
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId

data class SecurityState(
    private val context: AggregateContext,
    internal val users: MutableMap<UserId, User> = context.getMap("security/v1/users"),
    internal val groups: MutableMap<GroupId, Group> = context.getMap("security/v1/groups")
) : State {
    fun hasPermission(user: User, groupId: GroupId, permission: GroupPermission): Boolean {
        return getPermissions(user, groupId).contains(permission)
    }

    fun getPermissions(user: User, groupId: GroupId): Collection<GroupPermission> {
        val group: Group = groups[groupId] ?: return emptyList()
        if (user.isAdmin()) return allPermissions()

        val member = group.members.singleOrNull { it.userId == user.id } ?: return emptyList()
        return member.permissions
    }

    fun getGroups(): Collection<Group> = groups.values

    fun getGroups(user: User): Collection<Group> {
        return if (user.isAdmin()) groups.values
        else groups.values.filter { group ->
            group.members.any { member ->
                member.userId == user.id
            }
        }
    }

    fun findGroupById(id: GroupId): Group? {
        return groups[id]
    }

    fun getUsers(): Collection<User> = users.values

    fun findUserById(id: UserId): User? {
        return users[id]
    }

    fun findUserByUsername(username: String): User? {
        return users.values.singleOrNull { it.username == username }
    }

    fun groupHasMember(groupId: GroupId, userId: UserId): Boolean {
        return getGroupMembers(groupId).map { it.userId }.contains(userId)
    }

    fun getGroupMembers(groupId: GroupId) = groups[groupId]?.members ?: emptyList()
}