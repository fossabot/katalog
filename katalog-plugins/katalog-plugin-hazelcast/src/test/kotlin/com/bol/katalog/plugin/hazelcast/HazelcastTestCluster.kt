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

class HazelcastTestCluster(
    private vararg val members: String
) : AbstractTestCluster<HazelcastInstance, HazelcastAggregateContext>(*members) {
    private val log = KotlinLogging.logger {}

    private val eventStore: EventStore = InMemoryEventStore()
    private val clock: Clock = TestData.clock

    override fun addMemberAsync(memberId: String): Deferred<Node<HazelcastInstance, HazelcastAggregateContext>> {
        // Assume memberId is in the form "localhost:port" during tests
        require(memberId.startsWith("localhost:"))

        val config = HazelcastAutoConfiguration()
        val props = HazelcastProperties()
        props.port = memberId.split(":")[1].toInt()
        props.members = members.toList()
        props.instanceName = memberId

        val hazelcast = config.hazelcastInstance(props)

        val started = CompletableDeferred<Node<HazelcastInstance, HazelcastAggregateContext>>()
        val context = HazelcastAggregateContext(hazelcast, eventStore, clock)
        val node = Node(memberId, Thread.currentThread(), hazelcast, context)
        started.complete(node)
        return started
    }

    override fun removeMemberAsync(node: Node<HazelcastInstance, HazelcastAggregateContext>): Deferred<Unit> {
        return GlobalScope.async {
            node.clusterNode.shutdown()
        }
    }

    // Assume that Hazelcast is configured (in HazelcastAutoConfiguration) with a string attribute 'instanceName'
    // that is the same as the id's of the nodes in the 'nodes' map
    override fun getLeaderId(): String? {
        return if (nodes.values.isEmpty()) null
        else {
            val cluster = nodes.values.first().clusterNode.cluster
            val firstMember = cluster.members.first()
            log.info("Leader is: ${firstMember.getStringAttribute("instanceName")}")
            firstMember.getStringAttribute("instanceName")
        }
    }
}