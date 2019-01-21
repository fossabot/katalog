package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication.registry
import com.bol.katalog.TestData
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.readBlocking
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.throws
import java.util.*

class VersionAggregateTest : AbstractAggregateTest() {
    @Test
    fun `Can register versions`() {
        registry.readBlocking {
            expectThat(getVersions("id-ns1-schema1")).containsExactly(
                Version(
                    "id-ns1-schema1-v100",
                    TestData.clock.instant(),
                    Semver("1.0.0", Semver.SemverType.IVY)
                ),
                Version(
                    "id-ns1-schema1-v101",
                    TestData.clock.instant(),
                    Semver("1.0.1", Semver.SemverType.IVY)
                ),
                Version(
                    "id-ns1-schema1-v200snapshot",
                    TestData.clock.instant(),
                    Semver("2.0.0-SNAPSHOT", Semver.SemverType.IVY)
                )
            )

            expectThat(getVersions("id-ns2-schema3")).containsExactly(
                Version(
                    "id-ns2-schema3-v100",
                    TestData.clock.instant(),
                    Semver("1.0.0", Semver.SemverType.IVY)
                )
            )
        }
    }

    @Test
    fun `Can find schemas of versions`() {
        registry.readBlocking {
            expectThat(getVersions("id-ns1-schema1").map { getVersionSchemaId(it.id) }.distinct().single()).isEqualTo(
                "id-ns1-schema1"
            )
            expectThat(getVersions("id-ns2-schema3").map { getVersionSchemaId(it.id) }.distinct().single()).isEqualTo(
                "id-ns2-schema3"
            )
        }
    }

    @Test
    fun `Can delete version`() {
        val version = registry.readBlocking { getVersion("id-ns1-schema1-v100") }
        runBlocking {
            registry.send(DeleteVersionCommand("id-ns1-schema1-v100"))

            registry.read {
                expectThat(getVersions("id-ns1-schema1")).containsExactly(
                    Version(
                        "id-ns1-schema1-v101",
                        TestData.clock.instant(),
                        Semver("1.0.1", Semver.SemverType.IVY)
                    ),
                    Version(
                        "id-ns1-schema1-v200snapshot",
                        TestData.clock.instant(),
                        Semver("2.0.0-SNAPSHOT", Semver.SemverType.IVY)
                    )
                )

                expectThat(catching { getVersionSchemaId(version.id) }).throws<NotFoundException>()
            }
        }
    }

    @Test
    fun `Can get current major versions`() {
        val result =
            registry.readBlocking { getCurrentMajorVersions(listOf(v("1.0.0"), v("1.0.1"), v("2.0.0-SNAPSHOT"))) }
        expectThat(result.map { it.semVer.value }).containsExactly("2.0.0-SNAPSHOT", "1.0.1")
    }

    private fun v(version: String) =
        Version(
            UUID.randomUUID().toString(),
            TestData.clock.instant(),
            Semver(version, Semver.SemverType.IVY)
        )
}