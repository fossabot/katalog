package com.bol.katalog.features.registry

import com.bol.katalog.AggregateTester
import com.bol.katalog.TestData
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ConflictException
import com.bol.katalog.security.CreateUserCommand
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import strikt.api.expectThat
import strikt.assertions.containsExactly

class RegistryAggregateTest {
    private val tester = AggregateTester.of { ctx ->
        val security = createTestSecurityAggregate(ctx)
        RegistryAggregate(ctx, security, InMemoryBlobStore())
    }

    private fun createTestSecurityAggregate(ctx: AggregateContext): SecurityAggregate {
        val s = SecurityAggregate(ctx)
        runBlocking {
            s.send(CreateUserCommand("id-admin", "admin", null, setOf(SimpleGrantedAuthority("ROLE_ADMIN"))))
        }
        return s
    }

    @Test
    fun `Can create namespace`() {
        val ns = Namespace("id-ns1", "ns1", "id-group1", TestData.clock.instant())

        tester.run {
            send(CreateNamespaceCommand(ns.id, ns.groupId, ns.name))
            expect {
                event(NamespaceCreatedEvent(ns.id, ns.groupId, ns.name))
                state {
                    expectThat(it.getNamespaces()).containsExactly(ns)
                }
            }
        }
    }

    @Test
    fun `Cannot create existing namespace`() {
        tester.run {
            given(NamespaceCreatedEvent("id-ns1", "id-group1", "ns1"))
            send(CreateNamespaceCommand("id-ns1", "id-group1", "ns1"))
            expect {
                throws<ConflictException>()
            }
        }
    }

    @Test
    fun `Cannot delete existing namespace`() {
        tester.run {
            given(NamespaceCreatedEvent("id-ns1", "id-group1", "ns1"))
            send(DeleteNamespaceCommand("id-ns1"))
            expect {
                event(NamespaceDeletedEvent("id-ns1"))
            }
        }
    }
}