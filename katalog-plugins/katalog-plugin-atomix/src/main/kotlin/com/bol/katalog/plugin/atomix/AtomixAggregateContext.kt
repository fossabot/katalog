package com.bol.katalog.plugin.atomix

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.InMemoryCommandQueue
import com.bol.katalog.store.EventStore
import com.bol.katalog.users.UserId
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.atomix.cluster.MemberId
import io.atomix.core.Atomix
import io.atomix.primitive.Consistency
import io.atomix.protocols.backup.MultiPrimaryProtocol
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Clock
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KType

class AtomixAggregateContext(
    private val atomix: Atomix,
    private val eventStore: EventStore,
    private val clock: Clock
) : AggregateContext, AutoCloseable {
    private val log = KotlinLogging.logger {}

    private val mapper = jacksonObjectMapper()

    init {
        mapper.addMixIn(Command::class.java, TypedMixIn::class.java)
        mapper.addMixIn(Command.Result::class.java, TypedMixIn::class.java)
    }

    private val maps = ConcurrentHashMap<String, Map<*, *>>()
    private val queues = mutableMapOf<KType, InMemoryCommandQueue>()

    override fun <K, V> getMap(name: String): MutableMap<K, V> {
        @Suppress("UNCHECKED_CAST")
        return maps.getOrPut(name) {
            val protocol = MultiPrimaryProtocol.builder()
                .withConsistency(Consistency.LINEARIZABLE)
                .build()

            val map: Map<K, V> = atomix.mapBuilder<K, V>(name)
                .withProtocol(protocol)
                .build()

            map
        } as MutableMap<K, V>
    }

    override suspend fun <E : Event> persist(event: E, userId: UserId) =
        eventStore.store(event, userId, clock)

    override suspend fun <C : Command> send(
        handlerType: KType,
        command: C,
        metadata: Command.Metadata
    ): Command.Result {
        if (atomix.isLeader()) {
            // Are we local? Then we don't need to serialize the command
            return queues[handlerType]?.send(command, metadata)
                ?: throw IllegalStateException("Trying to send to handler type $handlerType that did not register an 'onCommand' handler yet")
        } else {
            // No, so send it through Atomix
            val serializable = SerializableCommand(command, metadata)
            val json = mapper.writeValueAsString(serializable)
            val leader = MemberId.from(atomix.getLeaderId())

            val serializableFailureResponse =
                atomix.communicationService.send<String, String>(handlerType.toString(), json, leader)
                    .asDeferred()

            val serializableFailure = mapper.readValue<SerializableResult>(serializableFailureResponse.await())
            return serializableFailure.result
        }
    }

    override fun onCommand(handlerType: KType, block: suspend (Command, Command.Metadata) -> Command.Result) {
        log.debug("Registered onCommand for '$handlerType'")

        // When receiving a message from Atomix, we'll put it in a local queue
        // This way we make sure that messages are processed sequentially
        val queue = InMemoryCommandQueue(block)
        queues[handlerType] = queue

        atomix.communicationService.subscribe<String, String>(handlerType.toString()) { message ->
            // Receive a serialized command
            val serializable = mapper.readValue<SerializableCommand<*>>(message)

            // Send it to local queue
            val result: Command.Result = runBlocking {
                queue.send(serializable.command, serializable.metadata)
            }

            // Serialize the result and send it as a reply to the sending Atomix node
            val serializableResult = mapper.writeValueAsString(SerializableResult(result))
            CompletableFuture.completedFuture(serializableResult)
        }
    }

    override fun close() {
        queues.values.forEach { it.close() }
    }

    data class SerializableCommand<C : Command>(val command: C, val metadata: Command.Metadata)
    data class SerializableResult(val result: Command.Result)

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    abstract class TypedMixIn
}