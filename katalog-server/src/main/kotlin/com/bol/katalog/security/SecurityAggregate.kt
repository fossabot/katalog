package com.bol.katalog.security

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.NotFoundException
import org.springframework.stereotype.Component

@Component
class SecurityAggregate : Aggregate<SecurityState>({ clustering -> SecurityState(clustering) }) {
    override fun getCommandHandler() = commandHandler {
        handle<CreateGroupCommand> {
            event(GroupCreatedEvent(command.id, command.name))
        }

        handle<DisableGroupCommand> {
            event(GroupDisabledEvent(command.id))
        }

        handle<CreateUserCommand> {
            event(UserCreatedEvent(command.id, command.username, command.encodedPassword, command.authorities))
        }

        handle<DisableUserCommand> {
            event(UserDisabledEvent(command.id))
        }

        handle<AddUserToGroupCommand> {
            if (!state.users.containsKey(command.userId)) throw NotFoundException()
            if (!state.groups.containsKey(command.groupId)) throw NotFoundException()

            event(UserAddedToGroupEvent(command.userId, command.groupId, command.permissions))
        }

        handle<RemoveUserFromGroupCommand> {
            if (!state.users.containsKey(command.userId)) throw NotFoundException()
            if (!state.groups.containsKey(command.groupId)) throw NotFoundException()

            event(UserRemovedFromGroupEvent(command.userId, command.groupId))
        }
    }

    override fun getEventHandler() = eventHandler {
        handle<GroupCreatedEvent> {
            state.groups[event.id] = Group(event.id, event.name, emptyList())
        }

        handle<GroupDisabledEvent> {
            state.groups.remove(event.id)
        }

        handle<UserCreatedEvent> {
            state.users[event.id] = User(event.id, event.username, event.encodedPassword, event.authorities)
        }

        handle<UserDisabledEvent> {
            state.groups.replaceAll { _, group ->
                group.copy(members = group.members.filterNot { member ->
                    member.userId == event.id
                })
            }
            state.users.remove(event.id)
        }

        handle<UserAddedToGroupEvent> {
            val group = state.groups[event.groupId]!!

            val newMember = GroupMember(event.userId, event.permissions)
            state.groups[event.groupId] = group.copy(members = group.members.plus(newMember))
        }

        handle<UserRemovedFromGroupEvent> {
            val group = state.groups[event.groupId]!!

            state.groups[event.groupId] =
                    group.copy(members = group.members.filterNot { member -> member.userId == event.userId })
        }
    }
}