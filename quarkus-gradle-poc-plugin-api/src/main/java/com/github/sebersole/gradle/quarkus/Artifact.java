package com.github.sebersole.gradle.quarkus;

/**
 * A wrapper around a (possibly not yet resolved) dependency that contains the extension
 */
public class Artifact {
	private Object dependencyNotation;

	public Artifact() {
	}

	public Artifact(String dependencyNotation) {
		setDependency( dependencyNotation );
	}

	public Object getDependency() {
		return dependencyNotation;
	}

	public void setDependency(Object notation) {
		this.dependencyNotation = notation;
	}

	@Override
	public String toString() {
		return "Artifact(`" + dependencyNotation + "`)";
	}
}
