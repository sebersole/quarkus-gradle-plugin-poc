package org.hibernate.build.gradle.quarkus.extension;

import org.hibernate.build.gradle.quarkus.QuarkusDsl;

/**
 * @author Steve Ebersole
 */
public class TransitiveExtension extends AbstractExtension {
	public TransitiveExtension(String dslContainerName, String gav, QuarkusDsl quarkusDsl) {
		super(
				dslContainerName,
				(extension, quarkusDsl1) -> new Artifact( gav, extension, quarkusDsl ),
				quarkusDsl
		);
	}
}
