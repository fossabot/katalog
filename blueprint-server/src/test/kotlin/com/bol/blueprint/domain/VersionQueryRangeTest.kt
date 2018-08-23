package com.bol.blueprint.domain

import com.bol.blueprint.queries.VersionRangeQuery
import com.vdurmont.semver4j.Semver
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VersionQueryRangeTest {
    private val query = VersionRangeQuery(
            listOf(
                    Version("1.0.0"),
                    Version("1.0.1"),
                    Version("1.1.0"),
                    Version("1.2.0"),
                    Version("1.0.2"), // later bugfix release
                    Version("2.0.0"),
                    Version("2.0.1-SNAPSHOT"),
                    Version("3.0.0-SNAPSHOT")
            ), Semver.SemverType.IVY
    )

    @Test
    fun `Can do basic version queries`() {
        assertThat(query.getVersionRange("1.0.1", "1.2.0")).containsExactly(
                Version("1.0.1"),
                Version("1.0.2"),
                Version("1.1.0")
        )
    }

    @Test
    fun `Can get major stable versions`() {
        assertThat(query.getLatestMajorVersions(stable = true)).containsExactly(
                Version("1.2.0"),
                Version("2.0.0")
        )
    }

    @Test
    fun `Can get major unstable versions`() {
        assertThat(query.getLatestMajorVersions(stable = false)).containsExactly(
                Version("2.0.1-SNAPSHOT"),
                Version("3.0.0-SNAPSHOT")
        )
    }

    @Test
    fun `Can get all major versions`() {
        assertThat(query.getLatestMajorVersions()).containsExactly(
                Version("1.2.0"),
                Version("2.0.1-SNAPSHOT"),
                Version("3.0.0-SNAPSHOT")
        )
    }
}