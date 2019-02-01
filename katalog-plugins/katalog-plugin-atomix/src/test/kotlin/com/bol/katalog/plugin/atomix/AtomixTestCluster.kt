package com.bol.katalog.plugin.atomix

import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import com.bol.katalog.testing.clustering.AbstractTestCluster
import io.atomix.core.Atomix
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asDeferred
import mu.KotlinLogging
import java.time.Clock
import kotlin.concurrent.thread

class AtomixTestCluster(
    private vararg val members: String
) : AbstractTestCluster<Atomix, AtomixAggregateContext>(*members) {
    private val log = KotlinLogging.logger {}
    private val eventStore: EventStore = InMemoryEventStore()
    private val clock: Clock = TestData.clock

    override fun addMemberAsync(memberId: String): Deferred<Node<Atomix, AtomixAggregateContext>> {
        log.debug("Starting node $memberId")
        val config = AtomixAutoConfiguration()
        val props = AtomixProperties()
        props.memberId = memberId
        props.members = members.toList()

        val started = CompletableDeferred<Node<Atomix, AtomixAggregateContext>>()
        thread {
            val atomix = config.atomix(props)

            atomix.start().thenApply {
                val context = AtomixAggregateContext(atomix, eventStore, clock)
                val node = Node(memberId, Thread.currentThread(), atomix, context)
                started.complete(node)
                log.debug("Started node $memberId")
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

    override fun getLeaderId(): String = nodes.values.first().clusterNode.getLeaderId()!!
}