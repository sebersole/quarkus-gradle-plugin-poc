package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;

/**
 * Basic config contract for a Quarkus extension
 */
public interface Extension extends Named {
	/**
	 * The name used for this Extension in the Quarkus DSL extensions container
	 */
	String getDslName();

	@Override
	default String getName() {
		return getDslName();
	}

	ResolvedDependency getArtifact();
	ResolvedDependency getDeploymentArtifact();

	Configuration getRuntimeDependencies();
	Configuration getDeploymentDependencies();
}
