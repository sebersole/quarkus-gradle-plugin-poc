package com.github.sebersole.gradle.quarkus.extension;

import java.io.Serializable;

import com.github.sebersole.gradle.quarkus.QuarkusDsl;

/**
 * Standard DSL implementation for configuring Quarkus extensions
 *
 * @author Steve Ebersole
 */
public class StandardExtension extends AbstractExtension implements Serializable {
	public StandardExtension(String dslContainerName, QuarkusDsl quarkusDsl) {
		super(
				dslContainerName,
				StandardExtension::extensionArtifact,
				quarkusDsl
		);
	}

	protected static Artifact extensionArtifact(Extension extension, QuarkusDsl quarkusDsl) {
		assert extension instanceof StandardExtension;

		return new Artifact( extension, quarkusDsl );
	}
}
