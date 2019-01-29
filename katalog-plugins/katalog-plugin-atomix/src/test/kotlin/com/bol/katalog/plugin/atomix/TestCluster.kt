package com.bol.katalog.plugin.atomix

import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import io.atomix.core.Atomix
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class TestCluster(
    private vararg val members: String
) : AutoCloseable {
    private val log = KotlinLogging.logger {}

    private val eventStore: EventStore = InMemoryEventStore()
    private val clock: Clock = TestData.clock

    private val nodes = ConcurrentHashMap<String, Node>()

    data class Node(
        val memberId: String,
        val thread: Thread,
        val atomix: Atomix,
        val context: AtomixAggregateContext
    )

    private fun addMember(memberId: String): Deferred<Unit> {
        log.debug("Starting node $memberId")
        val config = AtomixAutoConfiguration()
        val props = AtomixProperties()
        props.memberId = memberId
        props.members = members.toList()

        val started = CompletableDeferred<Unit>()
        thread {
            val atomix = config.atomix(props)

            atomix.start().thenApply {
                val context = AtomixAggregateContext(atomix, eventStore, clock)
                val node = Node(memberId, Thread.currentThread(), atomix, context)
                nodes[memberId] = node
                started.complete(Unit)
                log.debug("Started node $memberId")
            }
        }
        return started
    }

    fun run(block: suspend TestCluster.() -> Unit) {
        runBlocking {
            members.map { addMember(it) }.awaitAll()
            block()
        }

        close()
    }

    override fun close() {
        runBlocking {
            nodes.values.map {
                it.atomix.stop().asDeferred()
            }.awaitAll()
        }
    }

    suspend fun <T> onLeader(block: suspend Node.() -> T) = invokeOnNode(anyAtomix().getLeaderId()!!, block)

    fun <T> onAllMembers(block: suspend Node.() -> T) {
        invokeOnNodes(nodes.values.map { it.memberId }, block)
    }

    fun <T> onMember(memberId: String, block: suspend Node.() -> T) {
        invokeOnNodes(listOf(memberId), block)
    }

    private fun <T> invokeOnNodes(memberIds: List<String>, block: suspend Node.() -> T) {
        runBlocking {
            memberIds.map {
                val complete = CompletableDeferred<Unit>()
                thread {
                    runBlocking {
                        invokeOnNode(it, block)
                        complete.complete(Unit)
                    }
                }
                complete
            }.awaitAll()
        }
    }

    private suspend fun <T> invokeOnNode(memberId: String, block: suspend Node.() -> T): T {
        val node = nodes[memberId] ?: throw IllegalStateException("Unknown member '$memberId'")
        return block(node)
    }

    private fun anyAtomix() = nodes.values.first().atomix

    fun getLeaderId(): String = anyAtomix().getLeaderId()!!
}