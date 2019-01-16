package com.bol.katalog.cqrs

import kotlin.reflect.KClass

class CommandHandlerBuilder<S : State> {
    val handlers = mutableMapOf<KClass<*>, suspend CommandHandlerContext<*, *>.() -> Unit>()

    suspend fun invoke(context: CommandHandlerContext<S, Command>) {
        val handler = handlers[context.command::class]
            ?: throw UnsupportedOperationException("No handler found for command: ${context.command}")
        handler.invoke(context)
    }

    inline fun <reified T : Command> handle(crossinline handler: suspend CommandHandlerContext<S, T>.() -> Unit) {
        handlers[T::class] = {
            @Suppress("UNCHECKED_CAST")
            handler.invoke(this as CommandHandlerContext<S, T>)
        }
    }
}
