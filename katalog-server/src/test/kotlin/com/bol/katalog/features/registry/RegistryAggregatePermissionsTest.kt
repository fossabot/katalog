package com.bol.katalog.features.registry

import com.bol.katalog.TestData
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.admin
import com.bol.katalog.security.user1
import com.bol.katalog.security.userNoGroups
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class RegistryAggregatePermissionsTest {
    private val tester = RegistryTester.get()

    @Test
    fun `Can only read namespaces from own group`() {
        val ns1Owned = Namespace("id-owned-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
        val ns2Owned = Namespace("id-owned-ns2", "ns2", GroupId("id-group2"), TestData.clock.instant())
        val ns3Other = Namespace("id-other-ns3", "ns3", GroupId("id-group3"), TestData.clock.instant())

        tester.run {
            permissions {
                entities(ns1Owned, ns2Owned) {
                    allowRead(user1)
                }
                entities(ns1Owned, ns2Owned, ns3Other) {
                    allowAll(admin)
                }
            }
            send(ns1Owned.create())
            send(ns2Owned.create())
            send(ns3Other.create())
            expect {
                stateAs(user1) {
                    expectThat(it.getNamespaces()).containsExactly(ns1Owned, ns2Owned)
                }
                stateAs(userNoGroups) {
                    expectThat(it.getNamespaces()).isEmpty()
                }
                stateAs(admin) {
                    expectThat(it.getNamespaces()).containsExactly(ns1Owned, ns2Owned, ns3Other)
                }
            }
        }
    }

}