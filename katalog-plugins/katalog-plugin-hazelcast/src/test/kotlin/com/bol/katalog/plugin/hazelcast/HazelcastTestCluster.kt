package com.bol.katalog.plugin.hazelcast

import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import com.bol.katalog.testing.TestData
import com.bol.katalog.testing.clustering.AbstractTestCluster
import com.hazelcast.core.HazelcastInstance
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import mu.KotlinLogging
import java.time.Clock

class HazelcastTestCluster(private val size: Int) :
    AbstractTestCluster<HazelcastInstance, HazelcastAggregateContext>(size) {
    private val log = KotlinLogging.logger {}

    private val eventStore: EventStore = InMemoryEventStore()
    private val clock: Clock = TestData.clock

    override fun addMemberAsync(index: Int): Deferred<Node<HazelcastInstance, HazelcastAggregateContext>> {
        val config = HazelcastAutoConfiguration()
        val props = HazelcastProperties()
        props.port = 5701 + index
        props.instanceName = getMemberId(index)
        props.members = (0 until size).map { "localhost:${5701 + it}" }.toList()

        val hazelcast = config.hazelcastInstance(props)

        val started = CompletableDeferred<Node<HazelcastInstance, HazelcastAggregateContext>>()
        val context = HazelcastAggregateContext(hazelcast, eventStore, clock)
        val node = Node(index, Thread.currentThread(), hazelcast, context)
        started.complete(node)
        return started
    }

    override fun removeMemberAsync(node: Node<HazelcastInstance, HazelcastAggregateContext>): Deferred<Unit> {
        return GlobalScope.async {
            node.clusterNode.shutdown()
        }
    }

    override fun getLeaderIndex(): Int? {
        return if (nodes.isEmpty()) null
        else 0
    }

    override fun waitForCluster() = HazelcastAutoConfiguration().waitForCluster(nodes[0].clusterNode, size)
}
