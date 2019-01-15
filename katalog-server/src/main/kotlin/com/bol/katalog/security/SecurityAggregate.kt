package com.bol.katalog.security

import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.CommandHandlerBuilder.Companion.handleCommands
import com.bol.katalog.cqrs.events.EventHandler
import com.bol.katalog.cqrs.events.EventHandlerBuilder.Companion.handleEvents
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId
import org.springframework.stereotype.Component

@Component
class SecurityAggregate : EventHandler, CommandHandler, Resettable {
    private val users = mutableMapOf<UserId, User>()
    private val groups = mutableMapOf<GroupId, Group>()

    override val eventHandler
        get() = handleEvents {
            handle<GroupCreatedEvent> {
                groups[it.id] = Group(it.id, it.name, emptyList())
            }

            handle<GroupDisabledEvent> {
                groups.remove(it.id)
            }

            handle<UserCreatedEvent> {
                users[it.id] = User(it.id, it.username, it.encodedPassword, it.authorities)
            }

            handle<UserDisabledEvent> {
                groups.replaceAll { _, group ->
                    group.copy(members = group.members.filterNot { member ->
                        member.userId == it.id
                    })
                }
                users.remove(it.id)
            }

            handle<UserAddedToGroupEvent> {
                val group = groups[it.groupId]!!

                val newMember = GroupMember(it.userId, it.permissions)
                groups[it.groupId] = group.copy(members = group.members.plus(newMember))
            }

            handle<UserRemovedFromGroupEvent> {
                val group = groups[it.groupId]!!

                groups[it.groupId] =
                        group.copy(members = group.members.filterNot { member -> member.userId == it.userId })
            }
        }

    override val commandHandler
        get() = handleCommands {
            handle<CreateGroupCommand> {
                event(GroupCreatedEvent(command.id, command.name))
                complete()
            }

            handle<DisableGroupCommand> {
                event(GroupDisabledEvent(command.id))
                complete()
            }

            handle<CreateUserCommand> {
                event(UserCreatedEvent(command.id, command.username, command.encodedPassword, command.authorities))
                complete()
            }

            handle<DisableUserCommand> {
                event(UserDisabledEvent(command.id))
                complete()
            }

            handle<AddUserToGroupCommand> {
                if (!users.containsKey(command.userId)) throw NotFoundException()
                if (!groups.containsKey(command.groupId)) throw NotFoundException()

                event(UserAddedToGroupEvent(command.userId, command.groupId, command.permissions))
                complete()
            }

            handle<RemoveUserFromGroupCommand> {
                if (!users.containsKey(command.userId)) throw NotFoundException()
                if (!groups.containsKey(command.groupId)) throw NotFoundException()

                event(UserRemovedFromGroupEvent(command.userId, command.groupId))
                complete()
            }
        }

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

    override fun reset() {
        users.clear()
        groups.clear()
    }

    fun groupHasMember(groupId: GroupId, userId: UserId): Boolean {
        return getGroupMembers(groupId).map { it.userId }.contains(userId)
    }

    fun getGroupMembers(groupId: GroupId) = groups[groupId]?.members ?: emptyList()
}
