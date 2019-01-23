package com.bol.katalog.plugin.atomix

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.store.EventStore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.atomix.cluster.MemberId
import io.atomix.core.Atomix
import io.atomix.primitive.Consistency
import io.atomix.primitive.Replication
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
) : AggregateContext {
    private val log = KotlinLogging.logger {}

    private val mapper = jacksonObjectMapper()

    init {
        mapper.addMixIn(Command::class.java, TypedMixIn::class.java)
        mapper.addMixIn(Command.Result::class.java, TypedMixIn::class.java)
    }

    private val maps = ConcurrentHashMap<String, Map<*, *>>()

    override fun <K, V> getMap(name: String): MutableMap<K, V> {
        @Suppress("UNCHECKED_CAST")
        return maps.getOrPut(name) {
            val protocol = MultiPrimaryProtocol.builder()
                .withConsistency(Consistency.LINEARIZABLE)
                .withReplication(Replication.SYNCHRONOUS)
                .build()

            val map: Map<K, V> = atomix.mapBuilder<K, V>(name)
                .withProtocol(protocol)
                .build()

            map
        } as MutableMap<K, V>
    }

    override suspend fun <E : Event> persist(event: E, username: String?) =
        eventStore.store(event, username, clock)

    override suspend fun <C : Command> send(handlerType: KType, command: C): Command.Result {
        val serializable = SerializableCommand(command)
        val json = mapper.writeValueAsString(serializable)
        val leader = MemberId.from(atomix.getLeaderId())

        val serializableFailureResponse =
            atomix.communicationService.send<String, String>(handlerType.toString(), json, leader)
                .asDeferred()

        val serializableFailure = mapper.readValue<SerializableResult>(serializableFailureResponse.await())
        return serializableFailure.result
    }

    override fun onCommand(handlerType: KType, block: suspend (Command) -> Command.Result) {
        log.debug("Registered onCommand for '$handlerType'")
        atomix.communicationService.subscribe<String, String>(handlerType.toString()) { message ->
            val serializable = mapper.readValue<SerializableCommand<*>>(message)
            val result: Command.Result = runBlocking {
                block(serializable.command)
            }

            val serializableResult = mapper.writeValueAsString(SerializableResult(result))
            CompletableFuture.completedFuture(serializableResult)
        }
    }

    data class SerializableCommand<C : Command>(val command: C)
    data class SerializableResult(val result: Command.Result)

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    abstract class TypedMixIn
}