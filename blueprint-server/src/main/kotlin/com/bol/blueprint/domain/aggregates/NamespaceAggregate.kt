package com.bol.blueprint.domain.aggregates

import com.bol.blueprint.cqrs.Resettable
import com.bol.blueprint.cqrs.commands.CommandHandler
import com.bol.blueprint.cqrs.commands.CommandHandlerBuilder.Companion.handleCommands
import com.bol.blueprint.cqrs.events.EventHandler
import com.bol.blueprint.cqrs.events.EventHandlerBuilder.Companion.handleEvents
import com.bol.blueprint.domain.*
import org.springframework.stereotype.Component

@Component
class NamespaceAggregate : EventHandler, CommandHandler, Resettable {
    private val namespaces = mutableMapOf<NamespaceId, Namespace>()

    override val eventHandler
        get() = handleEvents {
            handle<NamespaceCreatedEvent> {
                namespaces[it.id] = Namespace(it.id, it.name, it.group)
            }
            handle<NamespaceDeletedEvent> {
                namespaces.remove(it.id)
            }
        }

    override val commandHandler
        get() = handleCommands {
            validate<CreateNamespaceCommand> {
                if (namespaces.values.any {
                        it.name == command.name || it.id == command.id
                    }) conflict()
                else valid()
            }

            validate<DeleteNamespaceCommand> {
                if (namespaces.containsKey(command.id)) valid()
                else notFound()
            }
        }

    override fun reset() {
        namespaces.clear()
    }

    /**
     * Get all available namespaces
     */
    fun getNamespaces(): Collection<Namespace> = namespaces.values

    /**
     * Get namespace based on id
     */
    fun getNamespace(namespaceId: NamespaceId): Namespace =
        namespaces[namespaceId] ?: throw NotFoundException("Could not find namespace with id: $namespaceId")

    fun findNamespace(namespace: String) = namespaces.values.firstOrNull { it.name == namespace }
        ?: throw NotFoundException("Could not find namespace: $namespace")
}