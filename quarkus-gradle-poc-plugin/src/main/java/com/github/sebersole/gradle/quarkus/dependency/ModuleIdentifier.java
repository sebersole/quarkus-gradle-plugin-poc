package com.github.sebersole.gradle.quarkus.dependency;

import java.util.Locale;

/**
 * Unique identifier for a module dependency
 */
public interface ModuleIdentifier {
	/**
	 * The dependency's group-id (`org.hibernate.orm` e.g.)
	 */
	String getGroupName();

	/**
	 * The dependency's artifact-id (`hibernate-core` e.g.)
	 */
	String getArtifactName();

	default String groupArtifact() {
		return String.format(
				Locale.ROOT,
				"%s:%s",
				getGroupName(),
				getArtifactName()
		);
	}
}
