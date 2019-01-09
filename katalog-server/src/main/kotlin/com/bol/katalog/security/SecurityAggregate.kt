package com.bol.katalog.security

import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.CommandHandlerBuilder.Companion.handleCommands
import com.bol.katalog.cqrs.events.EventHandler
import com.bol.katalog.cqrs.events.EventHandlerBuilder.Companion.handleEvents
import org.springframework.stereotype.Component

@Component
class SecurityAggregate : EventHandler, CommandHandler, Resettable {
    private val groups = mutableMapOf<GroupId, Group>()

    override val eventHandler
        get() = handleEvents {
            handle<GroupCreatedEvent> {
                groups[it.id] = Group(it.id, it.name, emptyList())
            }

            handle<UserAddedToGroupEvent> {
                val group = groups[it.groupId] ?: throw RuntimeException("Unknown groupId: ${it.groupId}")

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

            handle<AddUserToGroupCommand> {
                event(UserAddedToGroupEvent(command.userId, command.groupId, command.permissions))
                complete()
            }
        }

    override fun reset() {
        groups.clear()
    }

    fun hasPermission(user: KatalogUserDetails, groupId: GroupId, permission: GroupPermission): Boolean {
        return getPermissions(user, groupId).contains(permission)
    }

    fun getPermissions(user: KatalogUserDetails, groupId: GroupId): Collection<GroupPermission> {
        val group: Group = groups[groupId] ?: return emptyList()
        if (isAdmin(user)) return allPermissions()

        val member = group.members.singleOrNull { it.userId == user.getId() } ?: return emptyList()
        return member.permissions
    }

    fun getGroups(user: KatalogUserDetails): Collection<Group> {
        return if (isAdmin(user)) groups.values
        else groups.values.filter { group ->
            group.members.any { member ->
                member.userId == user.getId()
            }
        }
    }

    fun isAdmin(user: KatalogUserDetails): Boolean {
        return user.authorities.any { it.authority == "ROLE_ADMIN" }
    }
}
