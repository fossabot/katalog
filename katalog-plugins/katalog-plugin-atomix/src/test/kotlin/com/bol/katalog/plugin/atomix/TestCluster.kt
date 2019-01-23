package com.bol.katalog.plugin.atomix

import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import io.atomix.core.Atomix
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class TestCluster(
    private val eventStore: EventStore = InMemoryEventStore(),
    private val clock: Clock = TestData.clock
) : AutoCloseable {
    private val nodes = ConcurrentHashMap<String, Node>()

    data class Node(
        val memberId: String,
        val thread: Thread,
        val atomix: Atomix,
        val context: TestContext<AtomixAggregateContext>
    )

    private fun addNode(memberId: String): Deferred<Unit> {
        val config = AtomixAutoConfiguration()
        val props = AtomixProperties()
        props.memberId = memberId

        val started = CompletableDeferred<Unit>()
        thread {
            val atomix = config.atomix(props)

            atomix.start().thenApply {
                val context = TestContext(AtomixAggregateContext(atomix, eventStore, clock))
                val node = Node(memberId, Thread.currentThread(), atomix, context)
                nodes[memberId] = node
                started.complete(Unit)
            }
        }
        return started
    }

    suspend fun add(vararg memberIds: String) {
        memberIds.map { addNode(it) }.awaitAll()
    }

    private fun removeNode(memberId: String): Deferred<Void> {
        val node = nodes[memberId] ?: throw IllegalStateException("Unknown member '$memberId'")
        nodes.remove(memberId)
        return node.atomix.stop().asDeferred()
    }

    suspend fun remove(vararg memberIds: String) {
        memberIds.map { removeNode(it) }.awaitAll()
    }

    fun run(block: suspend TestCluster.() -> Unit) {
        runBlocking {
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

    suspend fun <T> onAllNodes(block: suspend Node.() -> T) {
        runBlocking {
            nodes.values.map {
                async {
                    block(it)
                }
            }
        }.awaitAll()
    }

    suspend fun <T> onLeader(block: suspend Node.() -> T) = invokeOnNode(anyAtomix().getLeaderId()!!, block)
    suspend fun <T> onRandomFollower(block: suspend Node.() -> T) =
        invokeOnNode(anyAtomix().getFollowerIds().first(), block)

    suspend fun <T> onAllFollowers(block: suspend Node.() -> T) {
        runBlocking {
            anyAtomix().getFollowerIds().map {
                async {
                    invokeOnNode(it, block)
                }
            }
        }.awaitAll()
    }

    private suspend fun <T> invokeOnNode(memberId: String, block: suspend Node.() -> T): T {
        val node = nodes[memberId] ?: throw IllegalStateException("Unknown member '$memberId'")
        return block(node)
    }

    private fun anyAtomix() = nodes.values.first().atomix
}