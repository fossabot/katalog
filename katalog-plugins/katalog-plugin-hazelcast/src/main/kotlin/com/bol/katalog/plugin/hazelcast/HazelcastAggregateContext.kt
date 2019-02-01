package com.bol.katalog.plugin.hazelcast

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.InMemoryCommandQueue
import com.bol.katalog.store.EventStore
import com.bol.katalog.users.UserId
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hazelcast.core.HazelcastInstance
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KType

class HazelcastAggregateContext(
    private val hazelcast: HazelcastInstance,
    private val eventStore: EventStore,
    private val clock: Clock
) : AggregateContext, AutoCloseable {
    private val log = KotlinLogging.logger {}

    private val maps = ConcurrentHashMap<String, MutableMap<*, *>>()
    private val queues = mutableMapOf<String, InMemoryCommandQueue>()

    private val mapper = jacksonObjectMapper()

    init {
        // Used by HandleCommandTask to link the HazelcastInstance to this HazelcastAggregateContext
        hazelcast.userContext[this::class.toString()] = this

        mapper.addMixIn(Command::class.java, TypedMixIn::class.java)
        mapper.addMixIn(Command.Result::class.java, TypedMixIn::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> getMap(name: String): MutableMap<K, V> = maps.getOrPut(name) {
        hazelcast.getMap<K, V>(name)
    } as MutableMap<K, V>

    override suspend fun <E : Event> persist(event: E, userId: UserId) = eventStore.store(event, userId, clock)

    override suspend fun <C : Command> send(
        handlerType: KType,
        command: C,
        metadata: Command.Metadata
    ): Command.Result {
        if (hazelcast.cluster.members.first() == hazelcast) {
            // Are we local? Then we don't need to serialize the command
            return queues[handlerType.toString()]?.send(command, metadata)
                ?: throw IllegalStateException("Trying to send to handler type $handlerType that did not register an 'onCommand' handler yet")
        } else {
            // No, so send it through Hazelcast
            val serializable = SerializableCommand(command, metadata)
            val json = mapper.writeValueAsString(serializable)
            val task = HandleCommandTask(handlerType.toString(), json)

            val leader = hazelcast.cluster.members.first()
            val serializableFailureResponse = hazelcast.getExecutorService(handlerType.toString())
                .submitToMember(task, leader)
                .asCompletableFuture()
                .asDeferred()

            val serializableFailure = mapper.readValue<SerializableResult>(serializableFailureResponse.await())
            return serializableFailure.result
        }
    }

    override fun onCommand(handlerType: KType, block: suspend (Command, Command.Metadata) -> Command.Result) {
        log.debug("Registered onCommand for '$handlerType' on: ${hazelcast.name}")

        // When receiving a message from Hazelcast, we'll put it in a local queue
        // This way we make sure that messages are processed sequentially
        val queue = InMemoryCommandQueue(block)
        queues[handlerType.toString()] = queue
    }

    override fun close() {
        queues.values.forEach { it.close() }
    }

    fun onJsonCommand(handlerType: String, message: String): String {
        // Receive a serialized command
        val serializable = mapper.readValue<HazelcastAggregateContext.SerializableCommand<*>>(message)

        // Send it to local queue
        val result: Command.Result = runBlocking {
            queues[handlerType]!!.send(serializable.command, serializable.metadata)
        }

        // Serialize the result and send it as a reply to the sending Hazelcast node
        return mapper.writeValueAsString(HazelcastAggregateContext.SerializableResult(result))
    }

    data class SerializableCommand<C : Command>(val command: C, val metadata: Command.Metadata)
    data class SerializableResult(val result: Command.Result)

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    abstract class TypedMixIn
}