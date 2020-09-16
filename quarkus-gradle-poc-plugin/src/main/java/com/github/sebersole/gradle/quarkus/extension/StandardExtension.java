package com.github.sebersole.gradle.quarkus.extension;

import java.io.Serializable;

import com.github.sebersole.gradle.quarkus.Artifact;
import com.github.sebersole.gradle.quarkus.Extension;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;

/**
 * Standard DSL implementation for configuring Quarkus extensions
 *
 * @author Steve Ebersole
 */
public class StandardExtension extends AbstractExtension implements Serializable {
	public StandardExtension(String dslContainerName, QuarkusDslImpl quarkusDsl) {
		super(
				dslContainerName,
				StandardExtension::extensionArtifact,
				StandardExtension::extensionArtifact,
				quarkusDsl
		);
	}

	protected static Artifact extensionArtifact(Extension extension, QuarkusDslImpl quarkusDsl) {
		assert extension instanceof StandardExtension;

		return new Artifact();
	}
}
