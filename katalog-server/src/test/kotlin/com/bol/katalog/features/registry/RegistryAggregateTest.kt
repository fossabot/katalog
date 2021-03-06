package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.ConflictException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.features.registry.support.*
import com.bol.katalog.security.GroupId
import com.bol.katalog.testing.TestData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class RegistryAggregateTest {
    private val tester = RegistryTester.get()

    private val ns1 = Namespace("id-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
    private val sc1 =
        Schema(
            "id-sc1",
            GroupId("id-group1"),
            ns1.id,
            TestData.clock.instant(),
            "sc1",
            SchemaType(versioningScheme = VersioningScheme.Semantic)
        )
    private val ver1 = Version("id-ver1", GroupId("id-group1"), sc1.id, TestData.clock.instant(), "1.0.0")

    private val ar1Data = byteArrayOf(1, 2, 3)
    private val ar1 = Artifact("id-ar1", ver1.groupId, ver1.id, "artifact.json", ar1Data.size, MediaType.JSON)

    @Nested
    inner class Namespaces {
        @Test
        fun `Can create`() {
            tester.run {
                send(ns1.create())
                expect {
                    event(ns1.created())
                    state<RegistryAggregate> {
                        expectThat(it.namespaces.getAll().toList()).containsExactly(ns1)
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
                send(sc1.create())
                expect {
                    event(sc1.created())
                    state<RegistryAggregate> {
                        expectThat(it.schemas.getByNamespaceId(ns1.id).toList()).containsExactly(sc1)
                    }
                }
            }
        }

        @Test
        fun `Cannot create without owner`() {
            tester.run {
                send(sc1.create())
                expect {
                    state<RegistryAggregate> {
                        throws<NotFoundException>("Unknown namespace id: id-ns1")
                    }
                }
            }
        }

        @Test
        fun `Cannot overwrite existing`() {
            tester.run {
                given(
                    ns1.created(),
                    sc1.created()
                )
                send(sc1.create())
                expect {
                    throws<ConflictException>("Schema already exists: sc1")
                }
            }
        }

        @Test
        fun `Can delete existing`() {
            tester.run {
                given(
                    ns1.created(),
                    sc1.created()
                )
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
                    sc1.created()
                )
                send(ver1.create())
                expect {
                    event(ver1.created())
                    state<RegistryAggregate> {
                        expectThat(it.versions.getAll(sc1.id).toList()).containsExactly(ver1)
                    }
                }
            }
        }

        @Test
        fun `Cannot create without owner`() {
            tester.run {
                send(ver1.create())
                expect {
                    state<RegistryAggregate> {
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
                    sc1.created(),
                    ver1.created()
                )
                send(ver1.create())
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
                    sc1.created(),
                    ver1.created()
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
                    sc1.created(),
                    ver1.created()
                )
                send(ar1.create(ar1Data))
                expect {
                    event(ar1.created(ar1Data))
                    state<RegistryAggregate> {
                        expectThat(it.artifacts.getByVersion(ver1.id).toList()).containsExactly(ar1)
                    }
                }
            }
        }

        @Test
        fun `Cannot create without owner`() {
            tester.run {
                send(ar1.create(byteArrayOf(1, 2, 3)))
                expect {
                    state<RegistryAggregate> {
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
                    sc1.created(),
                    ver1.created(),
                    ar1.created(ar1Data)
                )
                send(ar1.create(ar1Data))
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
                    sc1.created(),
                    ver1.created(),
                    ar1.created(ar1Data)
                )
                send(ar1.delete())
                expect {
                    event(ar1.deleted())
                }
            }
        }
    }
}