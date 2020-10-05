package com.github.sebersole.gradle.quarkus.dsl

import org.gradle.api.artifacts.Dependency

import com.github.sebersole.gradle.quarkus.service.BuildDetails
import com.github.sebersole.gradle.quarkus.Helper

/**
 * Used as the delegate for Groovy closures over the Quarkus platforms.
 */
class PlatformsConfigGroovyDelegate {
    private final BuildDetails buildDetails;

    PlatformsConfigGroovyDelegate(BuildDetails buildDetails) {
        this.buildDetails = buildDetails
    }

    Dependency quarkusUniverse() {
        def gav = Helper.groupArtifactVersion(
                Helper.QUARKUS_GROUP,
                Helper.QUARKUS_UNIVERSE_COMMUNITY_BOM,
                buildDetails.quarkusVersion
        )

        return buildDetails.addPlatform( gav )
    }

    Dependency platform(Object notation) {
        return buildDetails.addPlatform( notation )
    }
}
