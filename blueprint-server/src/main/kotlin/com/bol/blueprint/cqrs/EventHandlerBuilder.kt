package com.bol.blueprint.cqrs

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class EventHandlerBuilder {
    val handlers: MutableMap<String, (CompletedEventContext, Any) -> Unit> = mutableMapOf()

    inline fun <reified T : Any> handle(crossinline block: CompletedEventContext.(T) -> Unit) {
        handlers[T::class.java.name] = { handlerContext, event ->
            block.invoke(handlerContext, event as T)
        }
    }

    companion object {
        fun eventHandler(block: EventHandlerBuilder.() -> Unit): SendChannel<CompletedEvent<Any>> {
            val builder = EventHandlerBuilder()
            block.invoke(builder)

            val channel = Channel<CompletedEvent<Any>>()

            // Coroutine will exit when the channel closes
            GlobalScope.launch {
                for (message in channel) {
                    try {
                        val handler = builder.handlers[message.event::class.java.name]
                            ?: throw UnsupportedOperationException("Unknown event: $message.event")
                        handler.invoke(message.context, message.event)
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