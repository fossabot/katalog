package com.bol.katalog.cqrs2

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.State
import kotlin.reflect.KClass

class EventHandlerBuilder<S : State> {
    val handlers = mutableMapOf<KClass<*>, EventResponseContext<S, *>.() -> S>()

    fun invoke(context: EventResponseContext<S, Event>): S {
        val handler = handlers[context.event::class]
            ?: throw UnsupportedOperationException("No handler found for event: ${context.event}")
        return handler.invoke(context)
    }

    inline fun <reified T : Event> handle(crossinline handler: EventResponseContext<S, T>.() -> S) {
        handlers[T::class] = {
            @Suppress("UNCHECKED_CAST")
            handler.invoke(this as EventResponseContext<S, T>)
        }
    }
}
