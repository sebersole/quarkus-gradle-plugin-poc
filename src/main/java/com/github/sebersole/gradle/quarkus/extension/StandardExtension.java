package com.github.sebersole.gradle.quarkus.extension;

import java.io.Serializable;

import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Standard DSL implementation for configuring Quarkus extensions
 */
public class StandardExtension extends AbstractExtension implements Serializable {
	public StandardExtension(
			String dslContainerName,
			ResolvedDependency runtimeArtifact,
			ResolvedDependency deploymentArtifact,
			Configuration runtimeDependencies,
			Configuration deploymentDependencies,
			Services services) {
		super( dslContainerName, runtimeArtifact, deploymentArtifact, runtimeDependencies, deploymentDependencies, services );
	}
}
