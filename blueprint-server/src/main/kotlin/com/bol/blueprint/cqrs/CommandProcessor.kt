package com.bol.blueprint.cqrs

import com.bol.blueprint.cqrs.commands.CommandValidator
import com.bol.blueprint.cqrs.events.EventPublisher
import com.bol.blueprint.domain.Command
import com.bol.blueprint.domain.Event
import org.springframework.stereotype.Component

/**
 * The CommandProcessor is the class that is directly used by 'domain' processors.
 * If a command is valid, then some action will be taken that can emit one or more events
 */
@Component
class CommandProcessor(
    private val validator: CommandValidator,
    private val publisher: EventPublisher
) {
    class EventCollector {
        val events = arrayListOf<Event>()
        fun event(event: Event) = events.add(event)
    }

    suspend fun <TCommand : Command> ifValid(command: TCommand, block: suspend EventCollector.() -> Unit) {
        validator.validate(command)

        val collector = EventCollector()
        block.invoke(collector)
        collector.events.forEach {
            publisher.publish(it)
        }
    }
}