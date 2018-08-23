package com.bol.blueprint.queries

import com.bol.blueprint.domain.Version
import com.vdurmont.semver4j.Semver

class VersionRangeQuery(private val versions: Collection<Version>, private val semverType: Semver.SemverType) {
    fun getVersionRange(rangeStart: String?, rangeStop: String?): Collection<Version> {
        val semStart = rangeStart?.let { Semver(it, semverType) }
        val semStop = rangeStop?.let { Semver(it, semverType) }

        return versions.filter {
            (semStart?.isLowerThanOrEqualTo(it.version) ?: true) && (semStop?.isGreaterThan(it.version) ?: true)
        }.sortedBy { Semver(it.version) }
    }

    fun getLatestMajorVersions(stable: Boolean? = null): Collection<Version> {
        return versions
                .filter {
                    if (stable == null) {
                        true
                    } else Semver(it.version).isStable == stable
                }
                .sortedBy { Semver(it.version) }
                .associateBy { Semver(it.version, semverType).major }
                .values
    }
}