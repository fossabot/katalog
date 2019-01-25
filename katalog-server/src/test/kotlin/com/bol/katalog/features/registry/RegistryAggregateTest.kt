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

    private val ns1 = Namespace("id-ns1", "ns1", "id-group1", TestData.clock.instant())

    @Test
    fun `Can create namespace`() {
        tester.run {
            send(ns1.create())
            expect {
                event(ns1.created())
                state {
                    expectThat(it.getNamespaces()).containsExactly(ns1)
                }
            }
        }
    }

    @Test
    fun `Cannot create existing namespace`() {
        tester.run {
            given(ns1.created())
            send(ns1.create())
            expect {
                throws<ConflictException>()
            }
        }
    }

    @Test
    fun `Cannot delete existing namespace`() {
        tester.run {
            given(ns1.created())
            send(ns1.delete())
            expect {
                event(ns1.deleted())
            }
        }
    }
}