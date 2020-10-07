package com.github.sebersole.gradle.quarkus.dsl;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Dependency;

import groovy.lang.Closure;

/**
 * Standard DSL contract for defining and configuring an extension
 */
public interface ExtensionSpec extends Named {
	/**
	 * The name used for this Extension in the Quarkus DSL extensions container
	 */
	String getDslName();

	@Override
	default String getName() {
		return getDslName();
	}

	/**
	 * The artifact that defines the extension's runtime artifact
	 */
	Dependency getRuntimeArtifact();

	Dependency artifact(Object notation);
	void artifact(Object notation, Closure<Dependency> closure);
	void artifact(Object notation, Action<Dependency> action);
}
