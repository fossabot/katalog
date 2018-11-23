package com.bol.katalog.cqrs.events

import com.bol.katalog.domain.Event
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class EventHandlerBuilder {
    val handlers: MutableMap<String, (EventHandler.CompletedEventContext, Event) -> Unit> = mutableMapOf()

    inline fun <reified T : Event> handle(crossinline block: EventHandler.CompletedEventContext.(T) -> Unit) {
        handlers[T::class.java.name] = { handlerContext, event ->
            block.invoke(handlerContext, event as T)
        }
    }

    companion object {
        fun handleEvents(block: EventHandlerBuilder.() -> Unit): SendChannel<EventHandler.CompletedEvent<Event>> {
            val builder = EventHandlerBuilder()
            block.invoke(builder)

            val channel = Channel<EventHandler.CompletedEvent<Event>>()

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