package com.bol.katalog.cqrs

import kotlin.reflect.KClass

class EventHandlerBuilder<S : State> {
    val handlers = mutableMapOf<KClass<*>, suspend EventResponseContext<S, *>.() -> Unit>()

    suspend fun invoke(context: EventResponseContext<S, Event>) {
        val handler = handlers[context.event::class]
        handler?.invoke(context)
    }

    inline fun <reified T : Event> handle(crossinline handler: suspend EventResponseContext<S, T>.() -> Unit) {
        handlers[T::class] = {
            @Suppress("UNCHECKED_CAST")
            handler.invoke(this as EventResponseContext<S, T>)
        }
    }
}
