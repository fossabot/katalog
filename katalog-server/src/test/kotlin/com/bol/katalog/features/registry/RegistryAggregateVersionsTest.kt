package com.bol.katalog.features.registry

import com.bol.katalog.features.registry.support.RegistryTester
import com.bol.katalog.features.registry.support.create
import com.bol.katalog.features.registry.support.created
import com.bol.katalog.features.registry.support.delete
import com.bol.katalog.security.GroupId
import com.bol.katalog.support.TestData
import com.vdurmont.semver4j.Semver
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty

class RegistryAggregateVersionsTest {
    private val tester = RegistryTester.get()

    private val ns1 = Namespace("id-ns1", "ns1", GroupId("id-group1"), TestData.clock.instant())
    private val sc1 = Schema("id-sc1", TestData.clock.instant(), "sc1", SchemaType.default(), ns1)

    @Test
    fun `Can perform version logic`() {
        tester.run {
            given(ns1.created(), sc1.created())

            send(v("1.0.0").create())
            send(v("1.0.1").create())
            send(v("2.0.0").create())
            expect {
                state {
                    expectThat(it.versions.getCurrentMajorVersions(sc1.id).toList()).containsExactlyInAnyOrder(
                        v("1.0.1"),
                        v("2.0.0")
                    )
                }
            }

            send(v("1.0.1").delete())
            expect {
                state {
                    expectThat(it.versions.getCurrentMajorVersions(sc1.id).toList()).containsExactlyInAnyOrder(
                        v("1.0.0"),
                        v("2.0.0")
                    )
                }
            }

            send(sc1.delete())
            expect {
                state {
                    expectThat(it.versions.getCurrentMajorVersions(sc1.id).toList()).isEmpty()
                }
            }
        }
    }

    private fun v(version: String) =
        Version("id-v$version", TestData.clock.instant(), Semver(version, Semver.SemverType.NPM), sc1)
}