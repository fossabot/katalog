package com.bol.katalog.security

import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.hazelcast.HazelcastAggregate
import com.bol.katalog.cqrs.hazelcast.HazelcastAggregateContext
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class SecurityAggregate(context: HazelcastAggregateContext) : HazelcastAggregate(context) {
    init {
        setup {
            command<CreateGroupCommand> {
                event(GroupCreatedEvent(command.id, command.name))
            }

            command<DisableGroupCommand> {
                event(GroupDisabledEvent(command.id))
            }

            command<CreateUserCommand> {
                event(UserCreatedEvent(command.id, command.username, command.encodedPassword, command.authorities))
            }

            command<DisableUserCommand> {
                event(UserDisabledEvent(command.id))
            }

            command<AddUserToGroupCommand> {
                if (!getUsers().containsKey(command.userId)) throw NotFoundException("Unknown user id: $command.userId")
                if (!getGroups().containsKey(command.groupId)) throw NotFoundException("Unknown group id: $command.groupId")

                event(UserAddedToGroupEvent(command.userId, command.groupId, command.permissions))
            }

            command<RemoveUserFromGroupCommand> {
                if (!getUsers().containsKey(command.userId)) throw NotFoundException("Unknown user id: $command.userId")
                if (!getGroups().containsKey(command.groupId)) throw NotFoundException("Unknown group id: $command.groupId")

                event(UserRemovedFromGroupEvent(command.userId, command.groupId))
            }

            event<GroupCreatedEvent> {
                getMutableGroups()[event.id] = Group(event.id, event.name, emptyList())
            }

            event<GroupDisabledEvent> {
                getMutableGroups().remove(event.id)
            }

            event<UserCreatedEvent> {
                getMutableUsers()[event.id] = User(
                    event.id,
                    event.username,
                    event.encodedPassword,
                    event.authorities.map { SimpleGrantedAuthority(it) }.toSet()
                )
            }

            event<UserDisabledEvent> {
                val groups = getMutableGroups()

                val oldGroups = groups.values()
                oldGroups.forEach { oldGroup ->
                    val newGroup = oldGroup.copy(members = oldGroup.members.filterNot { member ->
                        member.userId == event.id
                    })
                    groups.replace(oldGroup.id, newGroup)
                }
                getMutableUsers().remove(event.id)
            }

            event<UserAddedToGroupEvent> {
                val groups = getMutableGroups()
                val group = groups.getForUpdate(event.groupId)!!

                val newMember = GroupMember(event.userId, event.permissions)
                groups[event.groupId] = group.copy(members = group.members.plus(newMember))
            }

            event<UserRemovedFromGroupEvent> {
                val groups = getMutableGroups()
                val group = groups.getForUpdate(event.groupId)!!

                groups[event.groupId] =
                    group.copy(members = group.members.filterNot { member -> member.userId == event.userId })
            }

        }
    }

    suspend fun getUsers() = context.map<UserId, User>("security/v1/users")
    private suspend fun getMutableUsers() = context.txMap<UserId, User>("security/v1/users")

    suspend fun getGroups() = context.map<GroupId, Group>("security/v1/groups")
    private suspend fun getMutableGroups() = context.txMap<GroupId, Group>("security/v1/groups")

    suspend fun findUserById(id: UserId): User? {
        if (id == SystemUser.get().id) {
            return SystemUser.get()
        }
        return getUsers()[id]
    }

    suspend fun findUserByUsername(username: String): User? {
        return getUsers().values.singleOrNull { it.username == username }
    }

    suspend fun findGroupById(id: GroupId): Group? {
        return getGroups()[id]
    }

    suspend fun getGroupsForUser(user: User): Collection<Group> {
        return if (user.isAdmin()) getGroups().values
        else getGroups().values.filter { group ->
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

    suspend fun getPermissions(user: User, groupId: GroupId): Collection<GroupPermission> {
        if (user.isAdmin()) return allPermissions()
        val group: Group = findGroupById(groupId) ?: return emptyList()

        val member = group.members.singleOrNull { it.userId == user.id } ?: return emptyList()
        return member.permissions
    }

    override suspend fun reset() {
        getMutableUsers().destroy()
        getMutableGroups().destroy()
    }
}