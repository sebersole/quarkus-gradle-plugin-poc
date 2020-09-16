package com.github.sebersole.gradle.quarkus;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import groovy.lang.Closure;

/**
 * Basic contract for an "extension config" object
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

	/**
	 * The artifact that contains the extension
	 */
	Artifact getArtifact();
	void artifact(Action<Artifact> artifactAction);
	Artifact artifact(Object notation);
	void artifact(Object notation, Closure<Artifact> artifactClosure);
	void artifact(Object notation, Action<Artifact> artifactAction);

	Artifact getDeploymentArtifact();
	void deploymentArtifact(Action<Artifact> action);
	Artifact deploymentArtifact(Object notation);
	void deploymentArtifact(Object notation, Closure<Artifact> artifactClosure);
	void deploymentArtifact(Object notation, Action<Artifact> artifactAction);

	Artifact quarkusArtifact(String artifactId);
	void quarkusArtifact(String artifactId, Closure<Artifact> artifactClosure);
	void quarkusArtifact(String artifactId, Action<Artifact> artifactAction);


	Configuration getRuntimeDependencies();
	Dependency runtimeDependency(Object notation);
	void runtimeDependency(Object notation, Closure<Dependency> closure);
	void runtimeDependency(Object notation, Action<Dependency> action);

	Configuration getDeploymentDependencies();
	Dependency deploymentDependency(Object notation);
	void deploymentDependency(Object notation, Closure<Dependency> closure);
	void deploymentDependency(Object notation, Action<Dependency> action);
}
