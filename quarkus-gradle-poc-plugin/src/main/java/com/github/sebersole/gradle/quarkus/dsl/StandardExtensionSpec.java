package com.github.sebersole.gradle.quarkus.dsl;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;

/**
 * Standard implementation of ExtensionConfig
 */
public class StandardExtensionSpec extends AbstractExtensionSpec {
	public StandardExtensionSpec(String name, BuildDetails buildDetails) {
		super( name, buildDetails );
	}

	public StandardExtensionSpec(String name, BuildDetails buildDetails, Object runtimeArtifactNotation) {
		super( name, buildDetails, runtimeArtifactNotation );
	}
}
