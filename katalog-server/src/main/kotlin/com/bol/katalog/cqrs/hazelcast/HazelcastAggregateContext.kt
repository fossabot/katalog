package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.hazelcast.views.MapContext
import com.bol.katalog.cqrs.hazelcast.views.MultiMapContext
import com.bol.katalog.security.CoroutineUserIdContext
import com.bol.katalog.store.EventStore
import com.bol.katalog.users.UserId
import com.hazelcast.core.ExecutionCallback
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import com.hazelcast.transaction.TransactionContext
import com.hazelcast.transaction.TransactionOptions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.io.Serializable
import java.time.Clock
import java.util.concurrent.Callable

open class HazelcastAggregateContext(
    protected val hazelcast: HazelcastInstance,
    private val eventStore: EventStore,
    private val clock: Clock
) {
    init {
        hazelcast.userContext[HazelcastAggregateContext::class.toString()] = this
    }

    private val queue = InMemoryCommandQueue { command, metadata ->
        handleEnqueuedCommand(command, metadata)
    }

    protected val aggregates = mutableListOf<HazelcastAggregate>()

    fun register(aggregate: HazelcastAggregate) {
        aggregates += aggregate
    }

    fun unregister(aggregate: HazelcastAggregate) {
        aggregates -= aggregate
    }

    fun getRegisteredAggregates() = aggregates.toList()

    suspend fun <C : Command> send(command: C) {
        val userId = CoroutineUserIdContext.get() ?: throw NullPointerException("No userId set")

        if (hazelcast.isLeader()) {
            queue.send(command, userId)
        } else {
            val task = RemoteCommandTask(command, userId)
            val executor = hazelcast.getExecutorService("aggregate-context-executor")

            val deferred = CompletableDeferred<Unit>()
            val callback = object : ExecutionCallback<Unit> {
                override fun onFailure(t: Throwable) {
                    deferred.completeExceptionally(t)
                }

                override fun onResponse(response: Unit) {
                    deferred.complete(response)
                }
            }

            executor.submitToMember(task, hazelcast.leader(), callback)
            deferred.await()
        }
    }

    suspend fun bulkSendLocalAs(userId: UserId, commands: List<Command>) {
        CoroutineUserIdContext.with(userId) {
            transaction(this) {
                commands.forEach {
                    handleCommand(it)
                }
            }
        }
    }

    open suspend fun <E : Event> publish(event: E) {
        val userId = CoroutineUserIdContext.get() ?: throw NullPointerException("No userId set")
        val persisted = eventStore.store(event, userId, clock)

        aggregates.forEach { agg ->
            agg.handleEvent(event, persisted.metadata)
        }
    }

    private fun handleRemoteCommand(command: Command, userId: UserId) {
        return runBlocking {
            queue.send(command, userId)
        }
    }

    private suspend fun handleEnqueuedCommand(command: Command, userId: UserId) {
        CoroutineUserIdContext.with(userId) {
            transaction(this) {
                handleCommand(command)
            }
        }
    }

    suspend fun handleCommand(command: Command) {
        // Send the command to all handlers until one of them has handled the command
        for (agg in aggregates) {
            agg.handleCommand(command)
        }
    }

    fun <K : Any, V : Any> map(name: String) = MapContext<K, V>(hazelcast, name)
    fun <K : Any, V : Any> multiMap(name: String) = MultiMapContext<K, V>(hazelcast, name)

    class RemoteCommandTask(val command: Command, val userId: UserId) : Callable<Unit>,
        Serializable, HazelcastInstanceAware {
        private lateinit var context: HazelcastAggregateContext

        override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
            context =
                hazelcastInstance.userContext[HazelcastAggregateContext::class.toString()] as HazelcastAggregateContext
        }

        override fun call() {
            return context.handleRemoteCommand(command, userId)
        }
    }

    fun newTransactionContext(options: TransactionOptions): TransactionContext =
        hazelcast.newTransactionContext(options)
}