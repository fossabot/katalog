package com.bol.blueprint.queries

import com.bol.blueprint.domain.Event

interface Sink {
    fun <T : Any> getHandler(): suspend (HandlerContext, T) -> Unit
}

data class HandlerContext(val metadata: Event.Metadata)

class SinkHandlerBuilder {
    val handlers: MutableMap<String, (HandlerContext, Any) -> Unit> = mutableMapOf()

    companion object {
        inline fun sinkHandler(block: SinkHandlerBuilder.() -> Unit): suspend (HandlerContext, T: Any) -> Unit {
            val sink = SinkHandlerBuilder()
            block.invoke(sink)

            return { handlerContext, event ->
                val handler = sink.handlers[event::class.java.name] ?: throw UnsupportedOperationException("Unknown event: $event")
                handler.invoke(handlerContext, event)
            }
        }
    }

    inline fun <reified T : Any> handle(crossinline block: HandlerContext.(T) -> Unit) {
        handlers[T::class.java.name] = { handlerContext, event ->
            block.invoke(handlerContext, event as T)
        }
    }
}