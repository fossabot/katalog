package com.bol.katalog.security

import com.bol.katalog.cqrs.AbstractAggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class SecurityAggregate(context: AggregateContext) : AbstractAggregate(context) {
    private val users = context.map<UserId, User>("security/v1/users")
    private val groups = context.map<GroupId, Group>("security/v1/groups")

    init {
        setup {
            command<CreateGroupCommand> {
                event(GroupCreatedEvent(command.id, command.name))
            }

            command<DisableGroupCommand> {
                event(GroupDisabledEvent(command.id))
            }

            command<CreateUserCommand> {
                event(
                    UserCreatedEvent(
                        command.id,
                        command.username,
                        command.encodedPassword,
                        command.authorities,
                        command.delegatedFromUserId
                    )
                )
            }

            command<DisableUserCommand> {
                event(UserDisabledEvent(command.id))
            }

            command<AddUserToGroupCommand> {
                if (!users.read { containsKey(command.userId) }) throw NotFoundException("Unknown user id: $command.userId")
                if (!groups.read { containsKey(command.groupId) }) throw NotFoundException("Unknown group id: $command.groupId")

                event(UserAddedToGroupEvent(command.userId, command.groupId, command.permissions))
            }

            command<RemoveUserFromGroupCommand> {
                if (!users.read { containsKey(command.userId) }) throw NotFoundException("Unknown user id: $command.userId")
                if (!groups.read { containsKey(command.groupId) }) throw NotFoundException("Unknown group id: $command.groupId")

                event(UserRemovedFromGroupEvent(command.userId, command.groupId))
            }

            event<GroupCreatedEvent> {
                groups.write { this[event.id] = Group(event.id, event.name, emptyList()) }
            }

            event<GroupDisabledEvent> {
                groups.write { remove(event.id) }
            }

            event<UserCreatedEvent> {
                users.write {
                    this[event.id] = User(
                        event.id,
                        event.username,
                        event.encodedPassword,
                        event.authorities.map { SimpleGrantedAuthority(it) }.toSet(),
                        event.delegatedFromUserId
                    )
                }
            }

            event<UserDisabledEvent> {
                val oldGroups = groups.read { values }
                oldGroups.forEach { oldGroup ->
                    val newGroup = oldGroup.copy(members = oldGroup.members.filterNot { member ->
                        member.userId == event.id
                    })
                    groups.write { replace(oldGroup.id, newGroup) }
                }
                users.write { remove(event.id) }
            }

            event<UserAddedToGroupEvent> {
                groups.write {
                    val group = getForUpdate(event.groupId)!!

                    val newMember = GroupMember(event.userId, event.permissions)
                    this[event.groupId] = group.copy(members = group.members.plus(newMember))
                }
            }

            event<UserRemovedFromGroupEvent> {
                groups.write {
                    val group = getForUpdate(event.groupId)!!

                    this[event.groupId] =
                        group.copy(members = group.members.filterNot { member -> member.userId == event.userId })
                }
            }

        }
    }

    suspend fun getAllUsers() = users.read { values }
    suspend fun getAllGroups() = groups.read { values }

    suspend fun findUserById(id: UserId?): User? {
        if (id == null) {
            return null
        }

        if (id == SystemUser.get().id) {
            return SystemUser.get()
        }
        return users.read { this[id] }
    }

    suspend fun findUserByUsername(username: String): User? {
        return users.read { values }.singleOrNull { it.username == username }
    }

    suspend fun findGroupById(id: GroupId): Group? {
        return groups.read { this[id] }
    }

    suspend fun getGroupsForUser(user: User): Collection<Group> {
        return if (user.isAdmin()) groups.read { values }
        else groups.read { values }.filter { group ->
            group.members.any { member ->
                member.userId == user.id
            }
        }
    }

    suspend fun getGroupMembers(groupId: GroupId) = findGroupById(groupId)?.members ?: emptyList()

    suspend fun groupHasMember(groupId: GroupId, userId: UserId): Boolean {
        return getGroupMembers(groupId).map { it.userId }.contains(userId)
    }

    suspend fun hasPermission(user: User, groupId: GroupId, permission: GroupPermission): Boolean {
        return getPermissions(user, groupId).contains(permission)
    }

    suspend fun hasPermission(userId: UserId, groupId: GroupId, permission: GroupPermission): Boolean {
        return getPermissions(userId, groupId).contains(permission)
    }

    suspend fun getPermissions(user: User, groupId: GroupId): Collection<GroupPermission> {
        if (user.isAdmin()) return allPermissions()

        val group: Group = findGroupById(groupId) ?: return emptyList()
        val member = group.members.singleOrNull { it.userId == user.id } ?: return emptyList()

        return if (user.delegatedFromUserId != null) {
            val delegatedUser = findUserById(user.delegatedFromUserId)
            if (delegatedUser == null) emptyList()
            else {
                // Permissions should never exceed the permissions of the delegatedMember
                val delegatedMember =
                    group.members.singleOrNull { it.userId == user.delegatedFromUserId } ?: return emptyList()
                member.permissions.intersect(delegatedMember.permissions)
            }
        } else {
            member.permissions
        }
    }

    suspend fun getPermissions(userId: UserId, groupId: GroupId): Collection<GroupPermission> {
        return findUserById(userId)?.let { user ->
            return getPermissions(user, groupId)
        } ?: emptyList()
    }

    override suspend fun reset() {
        users.reset()
        groups.reset()
    }
}