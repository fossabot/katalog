package com.bol.katalog.features.registry

import com.bol.katalog.AggregateTester
import com.bol.katalog.TestData
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ConflictException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.security.CreateUserCommand
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import strikt.api.expectThat
import strikt.assertions.containsExactly

class RegistryAggregateTest {
    val tester = AggregateTester.of { ctx ->
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
    private val sc1 = Schema("id-sc1", TestData.clock.instant(), "sc1", SchemaType.default())
    private val ver1 = Version("id-ver1", TestData.clock.instant(), Semver("1.0.0", Semver.SemverType.NPM))

    private val ar1Data = byteArrayOf(1, 2, 3)
    private val ar1 = Artifact("id-ar1", "artifact.json", ar1Data.size, MediaType.JSON)

    @Nested
    inner class Namespaces {
        @Test
        fun `Can create`() {
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
        fun `Cannot overwrite existing`() {
            tester.run {
                given(ns1.created())
                send(ns1.create())
                expect {
                    throws<ConflictException>()
                }
            }
        }

        @Test
        fun `Can delete existing`() {
            tester.run {
                given(ns1.created())
                send(ns1.delete())
                expect {
                    event(ns1.deleted())
                }
            }
        }
    }

    @Nested
    inner class Schemas {
        @Test
        fun `Can create`() {
            tester.run {
                given(ns1.created())
                send(sc1.create(ns1))
                expect {
                    event(sc1.created(ns1))
                    state {
                        expectThat(it.getSchemas()).containsExactly(sc1)
                    }
                }
            }
        }

        @Test
        fun `Cannot create without owner`() {
            tester.run {
                send(sc1.create(ns1))
                expect {
                    state {
                        throws<NotFoundException>("Unknown namespace id: id-ns1")
                    }
                }
            }
        }

        @Test
        fun `Cannot overwrite existing`() {
            tester.run {
                given(sc1.created(ns1))
                send(sc1.create(ns1))
                expect {
                    throws<ConflictException>("Schema already exists: sc1")
                }
            }
        }

        @Test
        fun `Can delete existing`() {
            tester.run {
                given(sc1.created(ns1))
                send(sc1.delete())
                expect {
                    event(sc1.deleted())
                }
            }
        }
    }

    @Nested
    inner class Versions {
        @Test
        fun `Can create`() {
            tester.run {
                given(
                    ns1.created(),
                    sc1.created(ns1)
                )
                send(ver1.create(sc1))
                expect {
                    event(ver1.created(sc1))
                    state {
                        expectThat(it.getVersions(sc1.id)).containsExactly(ver1)
                    }
                }
            }
        }

        @Test
        fun `Cannot create without owner`() {
            tester.run {
                send(ver1.create(sc1))
                expect {
                    state {
                        throws<NotFoundException>("Unknown schema id: id-sc1")
                    }
                }
            }
        }

        @Test
        fun `Cannot overwrite existing`() {
            tester.run {
                given(
                    ns1.created(),
                    sc1.created(ns1),
                    ver1.created(sc1)
                )
                send(ver1.create(sc1))
                expect {
                    throws<ConflictException>("Version already exists: 1.0.0")
                }
            }
        }

        @Test
        fun `Can delete existing`() {
            tester.run {
                given(
                    ns1.created(),
                    sc1.created(ns1),
                    ver1.created(sc1)
                )
                send(ver1.delete())
                expect {
                    event(ver1.deleted())
                }
            }
        }
    }

    @Nested
    inner class Artifacts {
        @Test
        fun `Can create`() {
            tester.run {
                given(
                    ns1.created(),
                    sc1.created(ns1),
                    ver1.created(sc1)
                )
                send(ar1.create(ver1, ar1Data))
                expect {
                    event(ar1.created(ver1, ar1Data))
                    state {
                        expectThat(it.getArtifacts(listOf(ver1.id))).containsExactly(ar1)
                    }
                }
            }
        }

        @Test
        fun `Cannot create without owner`() {
            tester.run {
                send(ar1.create(ver1, byteArrayOf(1, 2, 3)))
                expect {
                    state {
                        throws<NotFoundException>("Unknown version id: id-ver1")
                    }
                }
            }
        }

        @Test
        fun `Cannot overwrite existing`() {
            tester.run {
                given(
                    ns1.created(),
                    sc1.created(ns1),
                    ver1.created(sc1),
                    ar1.created(ver1, ar1Data)
                )
                send(ar1.create(ver1, ar1Data))
                expect {
                    throws<ConflictException>("Artifact already exists: artifact.json")
                }
            }
        }

        @Test
        fun `Can delete existing`() {
            tester.run {
                given(
                    ns1.created(),
                    sc1.created(ns1),
                    ver1.created(sc1),
                    ar1.created(ver1, ar1Data)
                )
                send(ar1.delete())
                expect {
                    event(ar1.deleted())
                }
            }
        }
    }
}