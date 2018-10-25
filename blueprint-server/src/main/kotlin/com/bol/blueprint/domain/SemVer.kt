package com.bol.blueprint.domain

import com.vdurmont.semver4j.Semver

fun SchemaType.toSemVerType() =
        when (this.versioningScheme) {
            VersioningScheme.Semantic -> Semver.SemverType.NPM
            VersioningScheme.Maven -> Semver.SemverType.IVY
        }