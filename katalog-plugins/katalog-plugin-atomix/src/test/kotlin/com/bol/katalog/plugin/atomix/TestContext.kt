package com.bol.katalog.plugin.atomix

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import kotlin.reflect.KType

class TestContext<T : AggregateContext>(private val actual: T) : AggregateContext by actual {
    val handledCommands = mutableListOf<Command>()

    override fun onCommand(handlerType: KType, block: suspend (Command) -> Command.Result) {
        actual.onCommand(handlerType) { command: Command ->
            val failure: Command.Result = block(command)
            handledCommands += command
            failure
        }
    }
}