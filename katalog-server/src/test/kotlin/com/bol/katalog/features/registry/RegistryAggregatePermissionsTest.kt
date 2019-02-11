package com.bol.katalog.features.registry

import com.bol.katalog.features.registry.support.RegistryTester
import com.bol.katalog.features.registry.support.create
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.support.admin
import com.bol.katalog.security.support.user1
import com.bol.katalog.security.support.userNoGroups
import com.bol.katalog.testing.TestData
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty

class RegistryAggregatePermissionsTest {
    private val tester = RegistryTester.get()

    private val ns1Owned = Namespace("id-owned-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
    private val ns2Owned = Namespace("id-owned-ns2", "ns2", GroupId("id-group2"), TestData.clock.instant())
    private val ns3Other = Namespace("id-other-ns3", "ns3", GroupId("id-group3"), TestData.clock.instant())

    @Test
    fun `Can only read namespaces from own group`() {
        tester.run {
            permissions {
                groups(ns1Owned.groupId, ns2Owned.groupId) {
                    allowRead(user1)
                }
                groups(ns1Owned.groupId, ns2Owned.groupId, ns3Other.groupId) {
                    allowAll(admin)
                }
            }
            send(ns1Owned.create())
            send(ns2Owned.create())
            send(ns3Other.create())
            expect {
                stateAs<RegistryAggregate>(user1) {
                    expectThat(it.namespaces.getAll().toList()).containsExactly(ns1Owned, ns2Owned)
                }
                stateAs<RegistryAggregate>(userNoGroups) {
                    expectThat(it.namespaces.getAll().toList()).isEmpty()
                }
                stateAs<RegistryAggregate>(admin) {
                    expectThat(it.namespaces.getAll().toList()).containsExactly(ns1Owned, ns2Owned, ns3Other)
                }
            }
        }
    }

    @Test
    fun `Can only read schemas from own group`() {
        val sc1Owned =
            Schema("id-sc1", ns1Owned.groupId, ns1Owned.id, TestData.clock.instant(), "sc1", SchemaType.default())
        val sc3Other =
            Schema("id-sc3", ns3Other.groupId, ns3Other.id, TestData.clock.instant(), "sc3", SchemaType.default())
        tester.run {
            permissions {
                groups(ns1Owned.groupId) {
                    allowRead(user1)
                    allowCreate(user1)
                }
                groups(ns1Owned.groupId, ns3Other.groupId) {
                    allowAll(admin)
                }
            }
            send(ns1Owned.create())
            send(ns3Other.create())
            sendAs(user1, sc1Owned.create())
            sendAs(admin, sc3Other.create())
            expect {
                stateAs<RegistryAggregate>(user1) {
                    expectThat(it.schemas.getAll().toList()).containsExactly(sc1Owned)
                }
                stateAs<RegistryAggregate>(userNoGroups) {
                    expectThat(it.schemas.getAll().toList()).isEmpty()
                }
                stateAs<RegistryAggregate>(admin) {
                    expectThat(it.schemas.getAll().toList()).containsExactlyInAnyOrder(sc1Owned, sc3Other)
                }
            }
        }
    }
}