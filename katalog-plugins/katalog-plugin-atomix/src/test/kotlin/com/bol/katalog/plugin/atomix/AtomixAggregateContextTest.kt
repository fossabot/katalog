package com.bol.katalog.plugin.atomix

import com.bol.katalog.testing.clustering.AbstractAggregateContextTests
import io.atomix.core.Atomix
import org.junit.jupiter.api.Test

class AtomixAggregateContextTest : AbstractAggregateContextTests<Atomix, AtomixAggregateContext>() {
    @Test
    fun `Commands are always routed to leader`() {
        super.commandsAreAlwaysRoutedToLeader(AtomixTestCluster("member-1", "member-2", "member-3"))
    }

    @Test
    fun `Can handle command failures`() {
        super.canHandleCommandFailures(AtomixTestCluster("member-1"))
    }
}
