package com.bol.blueprint.domain.readmodels

import com.bol.blueprint.cqrs.EventHandler
import com.bol.blueprint.cqrs.EventHandlerBuilder.Companion.eventHandler
import com.bol.blueprint.cqrs.Resettable
import com.bol.blueprint.domain.Namespace
import com.bol.blueprint.domain.NamespaceCreatedEvent
import com.bol.blueprint.domain.NamespaceDeletedEvent
import com.bol.blueprint.domain.NamespaceId
import org.springframework.stereotype.Component

@Component
class NamespaceReadModel : EventHandler, Resettable {
    private val namespaces = mutableMapOf<NamespaceId, Namespace>()

    private val handler = eventHandler {
        handle<NamespaceCreatedEvent> {
            namespaces[it.id] = Namespace(it.id, it.name, it.group)
        }
        handle<NamespaceDeletedEvent> {
            namespaces.remove(it.id)
        }
    }

    override fun getEventHandlerChannel() = handler

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