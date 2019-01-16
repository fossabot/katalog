package com.bol.katalog.cqrs2

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.cqrs.State
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.time.Clock

abstract class Aggregate<S : State>(
    val eventStore: EventStore,
    private val clock: Clock,
    private val initialState: S
) {
    private val log = KotlinLogging.logger {}

    private var state: S = initialState
    private var stateMutex = Mutex()

    private var commands = Channel<CommandWrapper>()
    private val commandsDone = CompletableDeferred<Unit>()

    data class CommandWrapper(
        val command: Command,
        val completion: CompletableDeferred<Unit> = CompletableDeferred()
    )

    fun reset() {
        state = initialState
    }

    suspend fun <T> read(block: S.() -> T) = stateMutex.withLock {
        block.invoke(state)
    }

    suspend fun send(command: Command): Deferred<Unit> {
        val wrapper = CommandWrapper(command)
        commands.send(wrapper)
        return wrapper.completion
    }

    fun start() {
        GlobalScope.launch {
            for (wrapper in commands) {
                try {
                    handleCommand(wrapper.command)
                    wrapper.completion.complete(Unit)
                } catch (e: Throwable) {
                    wrapper.completion.completeExceptionally(e)
                }
            }

            commandsDone.complete(Unit)
        }
    }

    suspend fun stop() {
        commands.close()
        commandsDone.await()
    }

    suspend fun replayFromStore() {
        var done = false
        var eventQuery = EventQuery()
        while (!done) {
            val page = eventStore.get(eventQuery)
            for (event in page.data) {
                handleEvent(event.data)
            }

            eventQuery = page.toEventQuery()
            done = page.data.isEmpty()
        }
    }

    private suspend fun handleCommand(command: Command) {
        log.debug("Command received: {}", command)

        val handler = getCommandHandler()
        val responseBuilder = CommandResponseBuilder(command)
        handler.invoke(responseBuilder)

        val response = responseBuilder.getResponse()
        response.events.forEach {
            persistEvent(it)
            handleEvent(it)
        }
    }

    private suspend fun persistEvent(event: Event) {
        val userDetails = CoroutineUserContext.get()
        val persistentEvent = PersistentEvent(
            metadata = PersistentEvent.Metadata(
                timestamp = clock.instant(),
                username = userDetails?.username ?: "unknown"
            ), data = event
        )

        eventStore.store(persistentEvent)
    }

    private suspend fun handleEvent(event: Event) {
        log.debug("Event received: {}", event)

        val handler = getEventHandler()

        stateMutex.withLock {
            val responseBuilder = EventResponseContext(state, event)
            val newState = handler.invoke(responseBuilder)
            if (newState != state) {
                state = newState
            }
        }
    }

    abstract fun getCommandHandler(): CommandHandlerBuilder

    protected fun commandHandler(block: CommandHandlerBuilder.() -> Unit): CommandHandlerBuilder {
        val builder = CommandHandlerBuilder()
        block.invoke(builder)
        return builder
    }

    abstract fun getEventHandler(): EventHandlerBuilder<S>

    protected fun eventHandler(block: EventHandlerBuilder<S>.() -> Unit): EventHandlerBuilder<S> {
        val builder = EventHandlerBuilder<S>()
        block.invoke(builder)
        return builder
    }
}