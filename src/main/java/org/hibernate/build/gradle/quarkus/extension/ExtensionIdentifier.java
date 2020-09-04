package org.hibernate.build.gradle.quarkus.extension;

import org.gradle.api.Named;
import org.gradle.api.Project;

import org.hibernate.build.gradle.quarkus.QuarkusDsl;

/**
 * @author Steve Ebersole
 */
public interface ExtensionIdentifier extends Named {
	@Override
	default String getName() {
		return getDslContainerName();
	}

	/**
	 * The name used for the Gradle {@link org.gradle.api.NamedDomainObjectContainer}.  This
	 * is the artifact-id minus the 'quarkus-' prefix (e.g. 'hibernate-orm' for
	 * 'quarkus-hibernate-orm')
	 */
	String getDslContainerName();

	/**
	 * The artifact id (in the Maven repo / GAV sense) of the Quarkus module.
	 */
	String getQuarkusArtifactId();

	/**
	 * A camel-case version of the name that can be used in building various build components
	 */
	String getCamelCaseName();

	static ExtensionIdentifier fromArtifactId(String artifactId, QuarkusDsl dsl) {
		assert artifactId.startsWith( "quarkus-" );

		return dsl.resolveExtensionIdentifier(
				artifactId,
				() -> new ExtensionIdentifierImpl( artifactId )
		);
	}

	static ExtensionIdentifier fromContainerName(String containerName, QuarkusDsl dsl) {
		assert ! containerName.startsWith( "quarkus-" );

		final String artifactId = "quarkus-" + containerName;

		return dsl.resolveExtensionIdentifier(
				artifactId,
				() -> new ExtensionIdentifierImpl( artifactId )
		);
	}
}
