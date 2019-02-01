package com.bol.katalog.plugin.hazelcast

import com.bol.katalog.testing.clustering.AbstractAggregateContextTests
import com.hazelcast.core.HazelcastInstance
import org.junit.jupiter.api.Test

class HazelcastAggregateContextTest : AbstractAggregateContextTests<HazelcastInstance, HazelcastAggregateContext>() {
    @Test
    fun `Commands are always routed to leader`() {
        super.commandsAreAlwaysRoutedToLeader(
            HazelcastTestCluster(
                "localhost:5701",
                "localhost:5702",
                "localhost:5703"
            )
        )
    }

    @Test
    fun `Can handle command failures`() {
        super.canHandleCommandFailures(HazelcastTestCluster("localhost:5701"))
    }
}
