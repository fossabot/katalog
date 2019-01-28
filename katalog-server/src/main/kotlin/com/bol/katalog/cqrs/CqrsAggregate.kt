package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import kotlin.reflect.KType
import kotlin.reflect.full.createType

abstract class CqrsAggregate<S : State>(
    private val context: AggregateContext,
    private var state: S
) : Aggregate<S>, AutoCloseable {
    private val log = KotlinLogging.logger {}
    private val handlerType: KType by lazy { this::class.createType() }

    init {
        context.onCommand(handlerType) { command ->
            handleCommand(command)
        }
    }

    private var stateMutex = Mutex()

    override fun close() {
    }

    override suspend fun <T> read(block: suspend S.() -> T) = stateMutex.withLock {
        block.invoke(state)
    }

    override suspend fun send(c: Command) {
        val failure = context.send(handlerType, c)
        when (failure) {
            is NotFoundFailure -> throw NotFoundException(failure.message)
            is ConflictFailure -> throw ConflictException(failure.message)
            else -> {
            }
        }
    }

    private suspend fun handleCommand(command: Command): Command.Result {
        log.debug("Command received in {}: {}", this, command)

        return stateMutex.withLock {
            invokeCommandHandler(command)
        }
    }

    private suspend fun invokeCommandHandler(command: Command): Command.Result {
        val handler = getCommandHandler()
        val handlerContext = CommandHandlerContext(state, command) {
            log.debug("`--> Required command in {}: {}", this, it)
            invokeCommandHandler(it)
            state
        }
        handler.invoke(handlerContext)

        val response = handlerContext.getResponse()
        if (response.failure != null) {
            return response.failure
        }

        response.events.forEach { event ->
            val persistent = context.persist(event, CoroutineUserContext.get()?.username)
            handleEvent(event, persistent.metadata)
        }

        return Command.Success
    }

    suspend fun <T : Event> handlePersistentEvent(event: PersistentEvent<T>) {
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