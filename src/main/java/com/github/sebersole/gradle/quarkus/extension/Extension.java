package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import com.github.sebersole.gradle.quarkus.QuarkusDsl;

import groovy.lang.Closure;

/**
 * Basic contract for an "extension config" object
 *
 * @author Steve Ebersole
 */
public interface Extension extends Named {
	/**
	 * The name used for this Extension in the {@link QuarkusDsl#getModules()} container
	 */
	String getDslName();

	/**
	 * Used by Gradle to determine the name of an Extension in the {@link QuarkusDsl#getModules()} container
	 * when directly adding one
	 */
	@Override
	default String getName() {
		return getDslName();
	}

	/**
	 * The artifact that contains the extension
	 */
	Artifact getArtifact();
	Artifact artifact(Action<Artifact> artifactAction);
	Artifact artifact(Object notation);
	Artifact artifact(Object notation, Closure<Artifact> artifactClosure);
	Artifact artifact(Object notation, Action<Artifact> artifactAction);
	Artifact quarkusArtifact(String artifactId);
	Artifact quarkusArtifact(String artifactId, Closure<Artifact> artifactClosure);
	Artifact quarkusArtifact(String artifactId, Action<Artifact> artifactAction);

	Configuration getDependencies();
	Dependency dependency(Object notation);
	Dependency dependency(Object notation, Closure<Dependency> closure);
	Dependency dependency(Object notation, Action<Dependency> action);
}
