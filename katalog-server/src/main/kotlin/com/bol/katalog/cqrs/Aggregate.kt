package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.clustering.ClusteringContext
import com.bol.katalog.cqrs.clustering.IAggregate
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging

abstract class Aggregate<S : State>(
    private val initialState: (ClusteringContext) -> S
): IAggregate {
    private val log = KotlinLogging.logger {}

    private var started = false
    private var stateMutex = Mutex()

    private lateinit var state: S
    private lateinit var clustering: ClusteringContext
    private lateinit var eventPersister: suspend (Event) -> PersistentEvent<Event>

    override fun getId() = this::class.simpleName!!

    fun setClusteringContext(clustering: ClusteringContext) {
        this.clustering = clustering
    }

    fun setEventPersister(eventPersister: suspend (Event) -> PersistentEvent<Event>) {
        this.eventPersister = eventPersister
    }

    suspend fun <T> read(block: suspend S.() -> T) = stateMutex.withLock {
        block.invoke(state)
    }

    suspend fun send(c: Command) {
        clustering.send(this, c).await()
    }

    internal fun start() {
        state = initialState(clustering)
        started = true
    }

    fun stop() {
        started = false
    }

    override suspend fun <T: Command> handleCommand(command: T) {
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
            val persistent = eventPersister(event)
            handleEvent(event, persistent.metadata)
        }
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