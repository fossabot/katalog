package com.bol.katalog.plugin.atomix

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class AtomixAutoConfigurationTest {
    @Test
    fun canFormCluster() {
        withClusterOfSize(3) { atomixen ->
            expectThat(atomixen[0].membershipService.reachableMembers.size).isEqualTo(3)
        }
    }
}