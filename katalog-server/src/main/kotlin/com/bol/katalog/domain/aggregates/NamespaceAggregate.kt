package com.bol.katalog.domain.aggregates

import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.CommandHandlerBuilder.Companion.handleCommands
import com.bol.katalog.cqrs.events.EventHandler
import com.bol.katalog.cqrs.events.EventHandlerBuilder.Companion.handleEvents
import com.bol.katalog.domain.*
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.groups.GroupService
import org.springframework.stereotype.Component

@Component
class NamespaceAggregate(private val groupService: GroupService) : EventHandler, CommandHandler, Resettable {
    private val namespaces = mutableMapOf<NamespaceId, Namespace>()

    override val eventHandler
        get() = handleEvents {
            handle<NamespaceCreatedEvent> {
                namespaces[it.id] = Namespace(it.id, it.name, it.group, metadata.timestamp)
            }
            handle<NamespaceDeletedEvent> {
                namespaces.remove(it.id)
            }
        }

    override val commandHandler
        get() = handleCommands {
            handle<CreateNamespaceCommand> {
                if (namespaces.values.any {
                        it.name == command.name || it.id == command.id
                    }) throw ConflictException()

                event(NamespaceCreatedEvent(command.id, command.group, command.name))
                complete()
            }

            handle<DeleteNamespaceCommand> {
                if (!namespaces.containsKey(command.id)) throw NotFoundException()

                event(NamespaceDeletedEvent(command.id))
                complete()
            }
        }

    override fun reset() {
        namespaces.clear()
    }

    /**
     * Get all available namespaces
     */
    suspend fun getNamespaces(): Collection<Namespace> = filteredForUser(namespaces.values)

    /**
     * Get namespace based on id
     */
    suspend fun getNamespace(namespaceId: NamespaceId): Namespace {
        val filtered = filteredForUser(listOfNotNull(namespaces[namespaceId]))
        return filtered.singleOrNull()
            ?: throw NotFoundException("Could not find namespace with id: $namespaceId")
    }

    suspend fun findNamespace(namespace: String): Namespace {
        val filtered = filteredForUser(namespaces.values)
        return filtered.firstOrNull { it.name == namespace }
            ?: throw NotFoundException("Could not find namespace: $namespace")
    }

    fun findUnauthorizedNamespace(namespace: String) = namespaces.values
        .firstOrNull { it.name == namespace }
        ?: throw NotFoundException("Could not find namespace: $namespace")

    // Filter the namespaces based on user, or remove them all if the user is null
    private suspend fun filteredForUser(namespaces: Collection<Namespace>): Collection<Namespace> {
        return CoroutineUserContext.get()?.let { user ->
            return namespaces.filter {
                groupService.hasGroupPermission(user, it.group, GroupPermission.READ)
            }
        } ?: emptyList()
    }
}
