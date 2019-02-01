package com.bol.katalog.plugin.atomix

import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import com.bol.katalog.testing.clustering.AbstractTestCluster
import io.atomix.core.Atomix
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import mu.KotlinLogging
import java.time.Clock
import kotlin.concurrent.thread

class AtomixTestCluster(private val size: Int) : AbstractTestCluster<Atomix, AtomixAggregateContext>(size) {
    private val log = KotlinLogging.logger {}
    private val eventStore: EventStore = InMemoryEventStore()
    private val clock: Clock = TestData.clock

    override fun addMemberAsync(index: Int): Deferred<Node<Atomix, AtomixAggregateContext>> {
        log.debug("Starting node $index")
        val config = AtomixAutoConfiguration()
        val props = AtomixProperties()
        props.memberId = getMemberId(index)
        props.members = (0 until size).map { getMemberId(it) }.toList()

        val started = CompletableDeferred<Node<Atomix, AtomixAggregateContext>>()
        thread {
            val atomix = config.atomix(props)

            atomix.start().thenApply {
                val context = AtomixAggregateContext(atomix, eventStore, clock)
                val node = Node(index, Thread.currentThread(), atomix, context)
                started.complete(node)
                log.debug("Started node $index")
            }
        }
        return started
    }

    override fun removeMemberAsync(node: AbstractTestCluster.Node<Atomix, AtomixAggregateContext>): Deferred<Unit> {
        return GlobalScope.async {
            node.clusterNode.stop().asDeferred().await()
            Unit
        }
    }

    override fun getLeaderIndex(): Int? = nodes.indexOfFirst { it.clusterNode.isLeader() }

    override fun waitForCluster() {
        while (true) {
            val actualSize = nodes[0].clusterNode.membershipService.reachableMembers.size
            if (actualSize != size) {
                log.info("Waiting for cluster to form (current size: $actualSize)")
                runBlocking { delay(1000) }
            } else {
                break
            }
        }
    }
}