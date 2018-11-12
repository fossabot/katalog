package com.bol.blueprint.cqrs.api

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class EventHandlerBuilder {
    val handlers: MutableMap<String, (EventHandler.CompletedEventContext, Any) -> Unit> = mutableMapOf()

    inline fun <reified T : Any> handle(crossinline block: EventHandler.CompletedEventContext.(T) -> Unit) {
        handlers[T::class.java.name] = { handlerContext, event ->
            block.invoke(handlerContext, event as T)
        }
    }

    companion object {
        fun eventHandler(block: EventHandlerBuilder.() -> Unit): SendChannel<EventHandler.CompletedEvent<Any>> {
            val builder = EventHandlerBuilder()
            block.invoke(builder)

            val channel = Channel<EventHandler.CompletedEvent<Any>>()

            // Coroutine will exit when the channel closes
            GlobalScope.launch {
                for (message in channel) {
                    try {
                        builder.handlers[message.event::class.java.name]?.invoke(message.context, message.event)
                        message.completed.complete(true)
                    } catch (e: Exception) {
                        message.completed.completeExceptionally(e)
                    }
                }
            }

            return channel
        }
    }
}