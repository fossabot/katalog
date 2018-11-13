package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.util.*

class VersionReadModelTest : AbstractReadModelTest() {
    @Test
    fun `Can register versions`() {
        expectThat(versions.getVersions(TestData.ns1_schema1)).containsExactly(
            Version(
                TestData.ns1_schema1_v100,
                TestData.clock.instant(),
                Semver("1.0.0", Semver.SemverType.IVY)
            ),
            Version(
                TestData.ns1_schema1_v101,
                TestData.clock.instant(),
                Semver("1.0.1", Semver.SemverType.IVY)
            ),
            Version(
                TestData.ns1_schema1_v200snapshot,
                TestData.clock.instant(),
                Semver("2.0.0-SNAPSHOT", Semver.SemverType.IVY)
            )
        )

        expectThat(versions.getVersions(TestData.ns2_schema3)).containsExactly(
            Version(
                TestData.ns2_schema3_v100,
                TestData.clock.instant(),
                Semver("1.0.0", Semver.SemverType.IVY)
            )
        )
    }

    @Test
    fun `Can find schemas of versions`() {
        expectThat(versions.getVersions(TestData.ns1_schema1).map { versions.getVersionSchemaId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns1_schema1
        )
        expectThat(versions.getVersions(TestData.ns2_schema3).map { versions.getVersionSchemaId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns2_schema3
        )
    }

    @Test
    fun `Can delete version`() {
        val version = versions.getVersion(TestData.ns1_schema1_v100)!!

        runBlocking {
            processor.deleteVersion(TestData.ns1_schema1_v100)
        }

        expectThat(versions.getVersions(TestData.ns1_schema1)).containsExactly(
            Version(
                TestData.ns1_schema1_v101,
                TestData.clock.instant(),
                Semver("1.0.1", Semver.SemverType.IVY)
            ),
            Version(
                TestData.ns1_schema1_v200snapshot,
                TestData.clock.instant(),
                Semver("2.0.0-SNAPSHOT", Semver.SemverType.IVY)
            )
        )

        expectThat(versions.getVersionSchemaId(version.id)).isNull()
    }

    @Test
    fun `Can get current major versions`() {
        val result = versions.getCurrentMajorVersions(listOf(v("1.0.0"), v("1.0.1"), v("2.0.0-SNAPSHOT")))
        expectThat(result.map { it.semVer.value }).containsExactly("2.0.0-SNAPSHOT", "1.0.1")
    }

    private fun v(version: String) =
        Version(
            UUID.randomUUID(),
            TestData.clock.instant(),
            Semver(version, Semver.SemverType.IVY)
        )
}