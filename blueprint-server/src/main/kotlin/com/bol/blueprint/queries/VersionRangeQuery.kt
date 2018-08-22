package com.bol.blueprint.queries

import com.bol.blueprint.domain.Version
import com.vdurmont.semver4j.Semver

class VersionRangeQuery(private val versions: Collection<Version>) {
    fun getVersionRange(rangeStart: String?, rangeStop: String?): List<Version> {
        val semStart = rangeStart?.let { Semver(it, Semver.SemverType.IVY) }
        val semStop = rangeStop?.let { Semver(it, Semver.SemverType.IVY) }

        return versions.filter {
            (semStart?.isLowerThanOrEqualTo(it.version) ?: true) && (semStop?.isGreaterThan(it.version) ?: true)
        }
    }
}