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
                    Version("2.0.2-SNAPSHOT"),
                    Version("3.0.0-SNAPSHOT")
            ), Semver.SemverType.IVY
    )

    @Test
    fun `Can do range queries`() {
        assertThat(query.getVersions(VersionRangeQuery.FilterOptions(rangeStart = "1.0.1", rangeStop = "1.2.0"))).containsExactly(
                Version("1.0.1"),
                Version("1.0.2"),
                Version("1.1.0")
        )
    }

    @Test
    fun `Can do stable queries`() {
        assertThat(query.getVersions(VersionRangeQuery.FilterOptions(stable = true))).containsExactly(
                Version("1.0.0"),
                Version("1.0.1"),
                Version("1.0.2"),
                Version("1.1.0"),
                Version("1.2.0"),
                Version("2.0.0")
        )
    }

    @Test
    fun `Can get major stable greatest versions`() {
        assertThat(query.getGreatestVersions(VersionRangeQuery.FilterOptions(stable = true)).values).containsExactly(
                Version("1.2.0"),
                Version("2.0.0")
        )
    }

    @Test
    fun `Can get major unstable greatest versions`() {
        assertThat(query.getGreatestVersions(VersionRangeQuery.FilterOptions(stable = false)).values).containsExactly(
                Version("2.0.2-SNAPSHOT"),
                Version("3.0.0-SNAPSHOT")
        )
    }

    @Test
    fun `Can get all major greatest versions`() {
        assertThat(query.getGreatestVersions().values).containsExactly(
                Version("1.2.0"),
                Version("2.0.2-SNAPSHOT"),
                Version("3.0.0-SNAPSHOT")
        )
    }

    @Test
    fun `Can get versions grouped by major version`() {
        assertThat(query.getGroupedVersions()).isEqualTo(
                mapOf<Int, Collection<Version>>(
                        1 to listOf(
                                Version("1.0.0"),
                                Version("1.0.1"),
                                Version("1.0.2"),
                                Version("1.1.0"),
                                Version("1.2.0")
                        ),
                        2 to listOf(
                                Version("2.0.0"),
                                Version("2.0.1-SNAPSHOT"),
                                Version("2.0.2-SNAPSHOT")
                        ),
                        3 to listOf(
                                Version("3.0.0-SNAPSHOT")
                        )
                )
        )
    }
}