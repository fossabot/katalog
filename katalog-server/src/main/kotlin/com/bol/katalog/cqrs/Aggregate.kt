package com.bol.katalog.cqrs

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging

abstract class Aggregate<S : State>(
    private val initialState: () -> S
) {
    private val log = KotlinLogging.logger {}

    private var started = false
    private var state: S = initialState()
    private var stateMutex = Mutex()

    private var commands = Channel<CommandWrapper>()
    private var commandsDone = CompletableDeferred<Unit>()

    private var commandListener: ((Command) -> Unit)? = null
    private var eventListener: EventListener = NoopEventListener()

    data class CommandWrapper(
        val commands: List<Command>,
        val completion: CompletableDeferred<Unit> = CompletableDeferred()
    )

    fun reset() {
        state = initialState()
        commands = Channel()
        commandsDone = CompletableDeferred()
    }

    suspend fun <T> read(block: suspend S.() -> T) = stateMutex.withLock {
        block.invoke(state)
    }

    suspend fun send(vararg c: Command) {
        if (!started) {
            throw RuntimeException("Message is being sent to aggregate that is not started")
        }

        if (commandListener != null) {
            c.forEach { commandListener!!(it) }
        }

        val wrapper = CommandWrapper(c.toList())
        commands.send(wrapper)
        wrapper.completion.await()
    }

    suspend fun sendDeferred(vararg c: Command): Deferred<Unit> {
        val wrapper = CommandWrapper(c.toList())
        commands.send(wrapper)
        return wrapper.completion
    }

    fun start() {
        GlobalScope.launch {
            started = true

            for (wrapper in commands) {
                try {
                    wrapper.commands.forEach {
                        handleCommand(it)
                    }
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
        started = false
    }

    fun setCommandListener(listener: ((Command) -> Unit)?) {
        commandListener = listener
    }

    fun setEventListener(listener: EventListener?) {
        eventListener = listener ?: NoopEventListener()
    }

    private suspend fun handleCommand(command: Command) {
        log.debug("Command received in {}: {}", this, command)

        stateMutex.withLock {
            invokeCommandHandler(command)
        }
    }

    private suspend fun invokeCommandHandler(command: Command) {
        val handler = getCommandHandler()
        val context = CommandHandlerContext(state, command) {
            log.debug("`--> Required command in {}: {}", this, it)
            invokeCommandHandler(it)
            state
        }
        handler.invoke(context)

        val response = context.getResponse()
        response.events.forEach { event ->
            val metadata = eventListener.beforeEventProcessed(event)
            handleEvent(event, metadata)
        }
    }

    internal suspend fun <T : Event> handlePersistentEvent(event: PersistentEvent<T>) {
        stateMutex.withLock {
            handleEvent(event.data, event.metadata)
        }
    }

    private suspend fun handleEvent(event: Event, metadata: PersistentEvent.Metadata) {
        log.debug("Event received in {}: {}", this, event)

        val handler = getEventHandler()

        val responseBuilder = EventResponseContext(state, event, metadata)
        handler.invoke(responseBuilder)
    }

    abstract fun getCommandHandler(): CommandHandlerBuilder<S>

    protected fun commandHandler(block: CommandHandlerBuilder<S>.() -> Unit): CommandHandlerBuilder<S> {
        val builder = CommandHandlerBuilder<S>()
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