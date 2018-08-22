package com.bol.blueprint.domain

import com.bol.blueprint.queries.VersionRangeQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VersionQueryRangeTest {
    private val query = VersionRangeQuery(
        listOf(
            Version("1.0.0"),
            Version("1.0.1"),
            Version("1.1.0"),
            Version("1.2.0"),
            Version("2.0.0")
        )
    )

    @Test
    fun `Can do basic version queries`() {
        assertThat(query.getVersionRange("1.0.1", "1.2.0")).containsExactly(
            Version("1.0.1"),
            Version("1.1.0")
        )
    }
}