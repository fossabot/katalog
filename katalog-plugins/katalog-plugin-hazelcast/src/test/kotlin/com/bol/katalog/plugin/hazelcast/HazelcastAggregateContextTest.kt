package com.bol.katalog.plugin.hazelcast

import com.bol.katalog.testing.clustering.AbstractAggregateContextTests
import com.hazelcast.core.HazelcastInstance
import org.junit.jupiter.api.Test

class HazelcastAggregateContextTest : AbstractAggregateContextTests<HazelcastInstance, HazelcastAggregateContext>() {
    @Test
    fun `Commands are always routed to leader`() {
        commandsAreAlwaysRoutedToLeader(HazelcastTestCluster(3))
    }

    @Test
    fun `Can handle command failures`() {
        canHandleCommandFailures(HazelcastTestCluster(1))
    }

    @Test
    fun `Can serialize entities`() {
        canSerialize(HazelcastTestCluster(2))
    }
}
