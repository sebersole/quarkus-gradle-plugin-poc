package org.hibernate.build.gradle.quarkus.extension;

import org.hibernate.build.gradle.quarkus.QuarkusDsl;

import groovy.lang.Closure;

/**
 * @author Steve Ebersole
 */
public class Artifact {
	private final Extension extension;
	private final QuarkusDsl quarkusDsl;

	private Object dependencyNotation;
	private Closure<Artifact> artifactClosure;

	public Artifact(Extension extension, QuarkusDsl quarkusDsl) {
		this.extension = extension;
		this.quarkusDsl = quarkusDsl;
	}

	public Artifact(String dependencyNotation, Extension extension, QuarkusDsl quarkusDsl) {
		this( extension, quarkusDsl );
		this.dependencyNotation = dependencyNotation;
	}

	/* package */ void apply(Object dependencyNotation, Closure<Artifact> artifactClosure) {
		this.dependencyNotation = dependencyNotation;
		this.artifactClosure = artifactClosure;
	}

	public Object getDependencyNotation() {
		return dependencyNotation;
	}

	public Closure<Artifact> getArtifactClosure() {
		return artifactClosure;
	}
}
