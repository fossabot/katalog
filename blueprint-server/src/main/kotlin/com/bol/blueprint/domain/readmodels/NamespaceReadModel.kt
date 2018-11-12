package com.bol.blueprint.domain.readmodels

import com.bol.blueprint.cqrs.Resettable
import com.bol.blueprint.cqrs.api.CommandHandler
import com.bol.blueprint.cqrs.api.CommandHandlerBuilder.Companion.commandHandler
import com.bol.blueprint.cqrs.api.EventHandler
import com.bol.blueprint.cqrs.api.EventHandlerBuilder.Companion.eventHandler
import com.bol.blueprint.domain.*
import org.springframework.stereotype.Component

@Component
class NamespaceReadModel : EventHandler, CommandHandler, Resettable {
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
            validate<CreateNamespaceCommand> { true }
            validate<DeleteNamespaceCommand> { true }
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