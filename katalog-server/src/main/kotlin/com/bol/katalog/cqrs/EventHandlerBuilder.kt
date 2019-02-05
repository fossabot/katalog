package com.bol.katalog.cqrs

import kotlin.reflect.KClass

class EventHandlerBuilder<S : State> {
    val handlers = mutableMapOf<KClass<*>, suspend EventResponseContext<S, *>.() -> Unit>()

    suspend fun invoke(context: EventResponseContext<S, Event>) {
        val handler = handlers[context.event::class]

        // If we don't have a handler for this event, that's fine. We'll just ignore it.
        handler?.invoke(context)
    }

    inline fun <reified T : Event> handle(crossinline handler: suspend EventResponseContext<S, T>.() -> Unit) {
        handlers[T::class] = {
            @Suppress("UNCHECKED_CAST")
            handler.invoke(this as EventResponseContext<S, T>)
        }
    }
}
