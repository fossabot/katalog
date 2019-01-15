package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication
import com.bol.katalog.TestApplication.processor
import com.bol.katalog.TestApplication.versions
import com.bol.katalog.TestData
import com.bol.katalog.cqrs.NotFoundException
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.throws
import java.util.*

class VersionAggregateTest {
    @Before
    fun before() {
        TestApplication.reset()
    }

    @Test
    fun `Can register versions`() {
        runBlocking {
            expectThat(versions.getVersions("id-ns1-schema1")).containsExactly(
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

            expectThat(versions.getVersions("id-ns2-schema3")).containsExactly(
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
        runBlocking {
            expectThat(versions.getVersions("id-ns1-schema1").map { versions.getVersionSchemaId(it.id) }.distinct().single()).isEqualTo(
                "id-ns1-schema1"
            )
            expectThat(versions.getVersions("id-ns2-schema3").map { versions.getVersionSchemaId(it.id) }.distinct().single()).isEqualTo(
                "id-ns2-schema3"
            )
        }
    }

    @Test
    fun `Can delete version`() {
        runBlocking {
            val version = versions.getVersion("id-ns1-schema1-v100")

            runBlocking {
                processor.apply(DeleteVersionCommand("id-ns1-schema1-v100"))
            }

            expectThat(versions.getVersions("id-ns1-schema1")).containsExactly(
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

            expectThat(catching { versions.getVersionSchemaId(version.id) }).throws<NotFoundException>()
        }
    }

    @Test
    fun `Can get current major versions`() {
        runBlocking {
            val result = versions.getCurrentMajorVersions(listOf(v("1.0.0"), v("1.0.1"), v("2.0.0-SNAPSHOT")))
            expectThat(result.map { it.semVer.value }).containsExactly("2.0.0-SNAPSHOT", "1.0.1")
        }
    }

    private fun v(version: String) =
        Version(
            UUID.randomUUID().toString(),
            TestData.clock.instant(),
            Semver(version, Semver.SemverType.IVY)
        )
}