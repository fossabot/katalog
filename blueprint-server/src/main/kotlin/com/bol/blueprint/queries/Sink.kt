package com.bol.blueprint.queries

import com.bol.blueprint.domain.Event

interface Sink {
    fun <T : Any> getHandler(): suspend (Event.Metadata, T) -> Unit
}

class SinkHandlerBuilder {
    val handlers: MutableMap<String, (Any) -> Unit> = mutableMapOf()

    companion object {
        inline fun sinkHandler(block: SinkHandlerBuilder.() -> Unit): suspend (Event.Metadata, T: Any) -> Unit {
            val sink = SinkHandlerBuilder()
            block.invoke(sink)

            return { _, event ->
                val handler = sink.handlers[event::class.java.name] ?: throw UnsupportedOperationException("Unknown event: $event")
                handler.invoke(event)
            }
        }
    }

    inline fun <reified T : Any> handle(crossinline block: (T) -> Unit) {
        handlers[T::class.java.name] = {
            block.invoke(it as T)
        }
    }
}