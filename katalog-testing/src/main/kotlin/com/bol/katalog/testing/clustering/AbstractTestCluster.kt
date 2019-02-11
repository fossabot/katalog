package com.bol.katalog.testing.clustering

/*
import com.bol.katalog.cqrs.AggregateContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

abstract class AbstractTestCluster<CLUSTER_NODE : Any, CLUSTER_CONTEXT : AggregateContext>(
    private val size: Int
) : AutoCloseable {
    protected val nodes = CopyOnWriteArrayList<Node<CLUSTER_NODE, CLUSTER_CONTEXT>>()

    data class Node<CLUSTER_NODE, CLUSTER_CONTEXT>(
        val index: Int,
        val thread: Thread,
        val clusterNode: CLUSTER_NODE,
        val context: CLUSTER_CONTEXT
    )

    protected abstract fun addMemberAsync(index: Int): Deferred<Node<CLUSTER_NODE, CLUSTER_CONTEXT>>
    protected abstract fun removeMemberAsync(node: Node<CLUSTER_NODE, CLUSTER_CONTEXT>): Deferred<Unit>
    protected abstract fun waitForCluster()
    abstract fun getLeaderIndex(): Int?

    fun run(block: suspend AbstractTestCluster<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> Unit) {
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

    suspend fun <T> onLeader(block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T) =
        block(nodes[getLeaderIndex()!!])

    fun <T> onAllMembers(block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T) {
        invokeOnNodes(nodes, block)
    }

    fun <T> onSingleFollower(block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T) {
        require(nodes.size > 1)
        invokeOnNodes(listOf(nodes.last()), block)
    }

    private fun <T> invokeOnNodes(
        nodes: List<Node<CLUSTER_NODE, CLUSTER_CONTEXT>>,
        block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T
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

    protected fun getMemberId(index: Int) = "member-$index"
}*/