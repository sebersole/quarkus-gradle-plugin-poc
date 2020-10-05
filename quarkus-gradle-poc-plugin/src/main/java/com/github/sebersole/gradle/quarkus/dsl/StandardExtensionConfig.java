package com.github.sebersole.gradle.quarkus.dsl;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;

/**
 * Standard implementation of ExtensionConfig
 */
public class StandardExtensionConfig extends AbstractExtensionConfig {
	public StandardExtensionConfig(String name, BuildDetails buildDetails) {
		super( name, buildDetails );
	}

	public StandardExtensionConfig(String name, BuildDetails buildDetails, Object runtimeArtifactNotation) {
		super( name, buildDetails, runtimeArtifactNotation );
	}
}
