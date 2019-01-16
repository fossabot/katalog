package com.bol.katalog.cqrs2

import com.bol.katalog.cqrs.Command
import kotlin.reflect.KClass

class CommandHandlerBuilder {
    val handlers = mutableMapOf<KClass<*>, CommandResponseBuilder<*>.() -> Unit>()

    fun invoke(context: CommandResponseBuilder<Command>) {
        val handler = handlers[context.command::class]
            ?: throw UnsupportedOperationException("No handler found for command: ${context.command}")
        handler.invoke(context)
    }

    inline fun <reified T : Command> handle(crossinline handler: CommandResponseBuilder<T>.() -> Unit) {
        handlers[T::class] = {
            @Suppress("UNCHECKED_CAST")
            handler.invoke(this as CommandResponseBuilder<T>)
        }
    }
}
