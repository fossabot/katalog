package com.bol.katalog.security

import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.CommandHandlerBuilder.Companion.handleCommands
import com.bol.katalog.cqrs.events.EventHandler
import com.bol.katalog.cqrs.events.EventHandlerBuilder.Companion.handleEvents
import com.bol.katalog.cqrs.NotFoundException
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

            handle<UserCreatedEvent> {
                users[it.id] = User(it.id, it.username, it.encodedPassword, it.authorities)
            }

            handle<UserAddedToGroupEvent> {
                val group = groups[it.groupId]!!

                val newMember = GroupMember(it.userId, it.permissions)
                groups[it.groupId] = group.copy(members = group.members.plus(newMember))
            }
        }

    override val commandHandler
        get() = handleCommands {
            handle<CreateGroupCommand> {
                event(GroupCreatedEvent(command.id, command.name))
                complete()
            }

            handle<CreateUserCommand> {
                event(UserCreatedEvent(command.id, command.username, command.encodedPassword, command.authorities))
                complete()
            }

            handle<AddUserToGroupCommand> {
                if (!users.containsKey(command.userId)) throw NotFoundException()
                if (!groups.containsKey(command.groupId)) throw NotFoundException()

                event(UserAddedToGroupEvent(command.userId, command.groupId, command.permissions))
                complete()
            }
        }

    override fun reset() {
        groups.clear()
    }

    fun hasPermission(user: User, groupId: GroupId, permission: GroupPermission): Boolean {
        return getPermissions(user, groupId).contains(permission)
    }

    fun getPermissions(user: User, groupId: GroupId): Collection<GroupPermission> {
        val group: Group = groups[groupId] ?: return emptyList()
        if (isAdmin(user)) return allPermissions()

        val member = group.members.singleOrNull { it.userId == user.id } ?: return emptyList()
        return member.permissions
    }

    fun getGroups(user: User): Collection<Group> {
        return if (isAdmin(user)) groups.values
        else groups.values.filter { group ->
            group.members.any { member ->
                member.userId == user.id
            }
        }
    }

    fun isAdmin(user: User): Boolean {
        return user.authorities.any { it.authority == "ROLE_ADMIN" }
    }

    fun findUserByUsername(username: String): User? {
        return users.values.singleOrNull { it.username == username }
    }

    fun findUserById(id: UserId): User? {
        return users[id]
    }
}
