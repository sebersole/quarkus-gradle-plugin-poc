package com.github.sebersole.gradle.quarkus.extension;

import java.io.Serializable;

import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Basic support for Extension implementations
 */
public abstract class AbstractExtension implements Extension, Serializable {
	private final String dslContainerName;
	private final Services services;

	private final ResolvedDependency runtimeArtifact;
	private final ResolvedDependency deploymentArtifact;

	private final Configuration runtimeDependencies;
	private final Configuration deploymentDependencies;

	public AbstractExtension(
			String dslContainerName,
			ResolvedDependency runtimeArtifact,
			ResolvedDependency deploymentArtifact,
			Configuration runtimeDependencies,
			Configuration deploymentDependencies,
			Services services) {
		this.dslContainerName = dslContainerName;
		this.runtimeArtifact = runtimeArtifact;
		this.deploymentArtifact = deploymentArtifact;
		this.runtimeDependencies = runtimeDependencies;
		this.deploymentDependencies = deploymentDependencies;
		this.services = services;
	}

	protected Services services() {
		return services;
	}

	@Override
	public String getDslName() {
		return dslContainerName;
	}

	public ResolvedDependency getArtifact() {
		return runtimeArtifact;
	}

	public ResolvedDependency getDeploymentArtifact() {
		return deploymentArtifact;
	}

	@Override
	public Configuration getRuntimeDependencies() {
		return runtimeDependencies;
	}

	@Override
	public Configuration getDeploymentDependencies() {
		return deploymentDependencies;
	}
}
