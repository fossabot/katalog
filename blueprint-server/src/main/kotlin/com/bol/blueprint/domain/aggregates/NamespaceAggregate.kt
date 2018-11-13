package com.bol.blueprint.domain.aggregates

import com.bol.blueprint.cqrs.Resettable
import com.bol.blueprint.cqrs.commands.CommandHandler
import com.bol.blueprint.cqrs.commands.CommandHandlerBuilder.Companion.commandHandler
import com.bol.blueprint.cqrs.events.EventHandler
import com.bol.blueprint.cqrs.events.EventHandlerBuilder.Companion.eventHandler
import com.bol.blueprint.domain.*
import org.springframework.stereotype.Component

@Component
class NamespaceAggregate : EventHandler, CommandHandler, Resettable {
    private val namespaces = mutableMapOf<NamespaceId, Namespace>()

    override val eventHandler
        get() = eventHandler {
            handle<NamespaceCreatedEvent> {
                namespaces[it.id] = Namespace(it.id, it.name, it.group)
            }
            handle<NamespaceDeletedEvent> {
                namespaces.remove(it.id)
            }
        }

    override val commandHandler
        get() = commandHandler {
            validate<CreateNamespaceCommand> {
                valid()
            }

            validate<DeleteNamespaceCommand> {
                valid()
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
    fun getNamespace(namespaceId: NamespaceId): Namespace? = namespaces[namespaceId]

    fun findNamespace(namespace: String) = namespaces.values.firstOrNull { it.name == namespace }
}