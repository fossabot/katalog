package com.bol.katalog.testing.clustering

import com.bol.katalog.cqrs.AggregateContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

abstract class AbstractTestCluster<CLUSTER_NODE : Any, CLUSTER_CONTEXT : AggregateContext>(
    private vararg val members: String
) : AutoCloseable {
    protected val nodes = ConcurrentHashMap<String, Node<CLUSTER_NODE, CLUSTER_CONTEXT>>()

    data class Node<CLUSTER_NODE, CLUSTER_CONTEXT>(
        val memberId: String,
        val thread: Thread,
        val clusterNode: CLUSTER_NODE,
        val context: CLUSTER_CONTEXT
    )

    protected abstract fun addMemberAsync(memberId: String): Deferred<Node<CLUSTER_NODE, CLUSTER_CONTEXT>>
    protected abstract fun removeMemberAsync(node: Node<CLUSTER_NODE, CLUSTER_CONTEXT>): Deferred<Unit>

    abstract fun getLeaderId(): String?

    fun run(block: suspend AbstractTestCluster<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> Unit) {
        runBlocking {
            members.map {
                addMemberAsync(it)
            }.awaitAll().forEach { node -> nodes[node.memberId] = node }
            block()
        }

        close()
    }

    override fun close() {
        runBlocking {
            nodes.values.map {
                removeMemberAsync(it)
            }.awaitAll()
        }
    }

    suspend fun <T> onLeader(block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T) =
        invokeOnNode(getLeaderId()!!, block)

    fun <T> onAllMembers(block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T) {
        invokeOnNodes(nodes.values.map { it.memberId }, block)
    }

    fun <T> onMember(memberId: String, block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T) {
        invokeOnNodes(listOf(memberId), block)
    }

    private fun <T> invokeOnNodes(memberIds: List<String>, block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T) {
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

    private suspend fun <T> invokeOnNode(
        memberId: String,
        block: suspend Node<CLUSTER_NODE, CLUSTER_CONTEXT>.() -> T
    ): T {
        val node = nodes[memberId] ?: throw IllegalStateException("Unknown member '$memberId'")
        return block(node)
    }
}