package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.bol.blueprint.queries.Query
import com.vdurmont.semver4j.Semver
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.util.*

class QueryTest {
    private val query = Query()

    @Test
    fun `Can get current major versions`() {
        val result = query.getCurrentMajorVersions(listOf(v("1.0.0"), v("1.0.1"), v("2.0.0-SNAPSHOT")))
        expectThat(result.map { it.semVer.value }).containsExactly("2.0.0-SNAPSHOT", "1.0.1")
    }

    private fun v(version: String) = Version(UUID.randomUUID(), TestData.clock.instant(), Semver(version, Semver.SemverType.IVY))
}