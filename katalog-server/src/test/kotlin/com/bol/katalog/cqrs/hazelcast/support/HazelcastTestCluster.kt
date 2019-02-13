package com.bol.katalog.cqrs.hazelcast.support

import com.bol.katalog.config.HazelcastAutoConfiguration
import com.bol.katalog.config.HazelcastProperties
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import com.bol.katalog.testing.TestData
import com.hazelcast.core.HazelcastInstance
import kotlinx.coroutines.*
import java.time.Clock
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

class HazelcastTestCluster(
    private val size: Int
) : AutoCloseable {
    private val eventStore: EventStore = InMemoryEventStore()
    private val clock: Clock = TestData.clock

    private val nodes = CopyOnWriteArrayList<Node>()

    data class Node(
        val index: Int,
        val thread: Thread,
        val clusterNode: HazelcastInstance,
        val context: AggregateContext
    )

    private fun addMemberAsync(index: Int): Deferred<Node> {
        val config = HazelcastAutoConfiguration()
        val props = HazelcastProperties()
        props.port = 5701 + index
        props.instanceName = getMemberId(index)
        props.members = (0 until size).map { "localhost:${5701 + it}" }.toList()

        val hazelcast = config.hazelcastInstance(props)

        val started = CompletableDeferred<Node>()
        val context = AggregateContext(hazelcast, eventStore, clock)
        val node = Node(index, Thread.currentThread(), hazelcast, context)
        started.complete(node)
        return started
    }

    private fun removeMemberAsync(node: Node): Deferred<Unit> {
        return GlobalScope.async {
            node.clusterNode.shutdown()
        }
    }

    private fun getLeaderIndex(): Int? {
        return if (nodes.isEmpty()) null
        else 0
    }

    private fun waitForCluster() = HazelcastAutoConfiguration().waitForCluster(nodes[0].clusterNode, size)

    fun run(block: suspend HazelcastTestCluster.() -> Unit) {
        runBlocking {
            (0 until size).map { addMemberAsync(it) }.awaitAll().forEach { nodes.add(it) }
            waitForCluster()
            block()
        }

        close()
    }

    override fun close() {
        runBlocking {
            nodes.map { removeMemberAsync(it) }.awaitAll()
        }
    }

    suspend fun <T> onLeader(block: suspend Node.() -> T) =
        block(nodes[getLeaderIndex()!!])

    fun <T> onAllMembers(block: suspend Node.() -> T) {
        invokeOnNodes(nodes, block)
    }

    fun <T> onSingleFollower(block: suspend Node.() -> T) {
        require(nodes.size > 1)
        invokeOnNodes(listOf(nodes.last()), block)
    }

    private fun <T> invokeOnNodes(
        nodes: List<Node>,
        block: suspend Node.() -> T
    ) {
        runBlocking {
            nodes.map {
                val complete = CompletableDeferred<Unit>()
                thread {
                    runBlocking {
                        block(it)
                        complete.complete(Unit)
                    }
                }
                complete
            }.awaitAll()
        }
    }

    private fun getMemberId(index: Int) = "member-$index"
}