package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.clustering.ClusteringContext
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging

abstract class Aggregate<S : State>(
    private val initialState: (ClusteringContext) -> S
) {
    private val log = KotlinLogging.logger {}

    private var started = false
    private var stateMutex = Mutex()

    private lateinit var state: S
    private lateinit var clustering: ClusteringContext
    private lateinit var eventPersister: EventPersister

    fun setClusteringContext(clustering: ClusteringContext) {
        this.clustering = clustering
    }

    fun setEventPersister(eventPersister: EventPersister) {
        this.eventPersister = eventPersister
    }

    suspend fun <T> read(block: suspend S.() -> T) = stateMutex.withLock {
        block.invoke(state)
    }

    suspend fun send(c: Command) {
        sendDeferred(c).await()
    }

    suspend fun sendDeferred(c: Command): Deferred<Unit> {
        if (!started) {
            throw RuntimeException("Message is being sent to aggregate that is not started")
        }

        return clustering.getClusteringChannel(this).sendCommand(c)
    }

    internal fun start() {
        val clusteringChannel = clustering.getClusteringChannel(this)
        val deferredStarted = CompletableDeferred<Unit>()
        state = initialState(clustering)

        GlobalScope.launch {
            started = true
            deferredStarted.complete(Unit)

            for (handleable in clusteringChannel.getChannel()) {
                handleable.handle {
                    handleCommand(it)
                }
            }

            clusteringChannel.notifyChannelClosed()
        }

        runBlocking {
            deferredStarted.await()
        }
    }

    fun stop() {
        started = false
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
            val persistent = eventPersister.persist(event)
            handleEvent(event, persistent.metadata)
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