package com.github.sebersole.gradle.quarkus.extension;

import com.github.sebersole.gradle.quarkus.QuarkusDsl;

/**
 * A wrapper around a (possibly not yet resolved) dependency that contains the extension
 *
 * @author Steve Ebersole
 */
public class Artifact {
	private final Extension extension;
	private final QuarkusDsl quarkusDsl;
	private Object dependencyNotation;

	public Artifact(Extension extension, QuarkusDsl quarkusDsl) {
		this.extension = extension;
		this.quarkusDsl = quarkusDsl;
	}

	public Artifact(String dependencyNotation, Extension extension, QuarkusDsl quarkusDsl) {
		this( extension, quarkusDsl );
		setDependency( dependencyNotation );
	}

	public Object getDependency() {
		return dependencyNotation;
	}

	void setDependency(Object notation) {
		quarkusDsl.getProject().getLogger().lifecycle(
				"Setting Extension (`{}`) Artifact dependency notation : {}",
				extension.getName(),
				notation
		);
		this.dependencyNotation = notation;
	}

	@Override
	public String toString() {
		return "Artifact(`" + dependencyNotation + "`)";
	}
}
