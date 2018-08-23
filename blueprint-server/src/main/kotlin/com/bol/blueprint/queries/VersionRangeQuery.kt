package com.bol.blueprint.queries

import com.bol.blueprint.domain.Version
import com.vdurmont.semver4j.Semver

class VersionRangeQuery(private val versions: Collection<Version>, private val semverType: Semver.SemverType) {
    data class FilterOptions(
            val rangeStart: String? = null,
            val rangeStop: String? = null,
            val stable: Boolean? = null
    )

    /**
     * Gets all versions, filtered
     */
    fun getVersions(filterOptions: FilterOptions = FilterOptions()): Collection<Version> {
        return filtered(versions, filterOptions)
                .sortedBy { Semver(it.version) }
    }

    /**
     * Gets all the greatest versions per major version, filtered
     */
    fun getGreatestVersions(filterOptions: FilterOptions = FilterOptions()): Map<Int, Version> {
        return getVersions(filterOptions)
                .associateBy { Semver(it.version, semverType).major }
    }

    /**
     * Gets all the versions, grouped by major version, filtered
     */
    fun getGroupedVersions(filterOptions: FilterOptions = FilterOptions()): Map<Int, Collection<Version>> {
        return getVersions(filterOptions)
                .groupBy { Semver(it.version, semverType).major }
    }

    private fun filtered(versions: Collection<Version>, options: FilterOptions): Collection<Version> {
        val semStart = options.rangeStart?.let { Semver(it, semverType) }
        val semStop = options.rangeStop?.let { Semver(it, semverType) }

        return versions.filter {
            // Filter 'range*'
            (semStart?.isLowerThanOrEqualTo(it.version) ?: true) && (semStop?.isGreaterThan(it.version) ?: true)
        }.filter {
            // Filter 'stable'
            if (options.stable == null) {
                true
            } else {
                Semver(it.version).isStable == options.stable
            }
        }
    }
}
