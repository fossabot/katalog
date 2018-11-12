package com.bol.blueprint.queries

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class SinkHandlerBuilder {
    val handlers: MutableMap<String, (HandlerContext, Any) -> Unit> = mutableMapOf()

    inline fun <reified T : Any> handle(crossinline block: HandlerContext.(T) -> Unit) {
        handlers[T::class.java.name] = { handlerContext, event ->
            block.invoke(handlerContext, event as T)
        }
    }

    companion object {
        fun sinkHandler(block: SinkHandlerBuilder.() -> Unit): SendChannel<HandlerMessage<Any>> {
            val builder = SinkHandlerBuilder()
            block.invoke(builder)

            val channel = Channel<HandlerMessage<Any>>()

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