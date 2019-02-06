package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserIdContext
import com.bol.katalog.users.UserId
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
        context.register(this)
        context.onCommand(handlerType) { command, metadata -> handleCommand(command, metadata) }
    }

    override fun close() {
    }

    override suspend fun <T> readAs(userId: UserId, block: suspend S.() -> T) = CoroutineUserIdContext.with(userId) {
        context.withReadLock {
            block.invoke(state)
        }
    }

    override suspend fun sendAs(userId: UserId, c: Command) {
        val metadata = Command.Metadata(userId)
        val result = context.send(handlerType, c, metadata)

        when (result) {
            is Command.Result.Success -> {
            }
            is Command.Result.Unhandled -> {
            }
            is Command.Result.Failure -> throw result.asThrowable()
        }.let {}
    }

    internal suspend fun handleCommand(command: Command, metadata: Command.Metadata): Command.Result {
        return try {
            CoroutineUserIdContext.with(metadata.userId) {
                handleCommandInternal(command, metadata)
            }
        } catch (e: Throwable) {
            // If an exception was thrown during the handling of a command,
            // convert it to a command failure result
            e.asCommandFailure()
        }
    }

    private suspend fun handleCommandInternal(
        command: Command,
        metadata: Command.Metadata
    ): Command.Result {
        log.debug("Command received in {}: {} (metadata: {})", this, command, metadata)

        val handler = getCommandHandler()
        val handlerContext = CommandHandlerContext(state, command, metadata) {
            log.debug("`--> Required command in {}: {}", this, it)
            context.require(it, metadata)
            state
        }

        return if (handler.invoke(handlerContext)) {
            val response = handlerContext.getResponse()
            return if (response.failure != null) {
                response.failure
            } else {
                response.events.forEach { event ->
                    context.publish(event, metadata.userId)
                }

                Command.Result.Success
            }
        } else {
            Command.Result.Unhandled
        }
    }

    internal suspend fun handleEvent(event: Event, metadata: PersistentEvent.Metadata) {
        log.debug("Event received in {}: {}", this, event)

        CoroutineUserIdContext.with(metadata.userId) {
            val handler = getEventHandler()

            val responseBuilder = EventResponseContext(state, event, metadata)
            handler.invoke(responseBuilder)
        }
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

    override fun directAccess() = DirectCqrsAggregate(this)

    fun unregister() {
        context.unregister(this)
    }
}