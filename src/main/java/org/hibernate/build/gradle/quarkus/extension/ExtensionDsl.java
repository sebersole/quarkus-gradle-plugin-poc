package org.hibernate.build.gradle.quarkus.extension;

import java.util.Map;

import org.gradle.api.artifacts.Configuration;

/**
 * Basic contract for an "extension config" object
 *
 * @author Steve Ebersole
 */
public interface ExtensionDsl {
	ExtensionIdentifier getIdentifier();
	Configuration getDependencyConfiguration();

	default String getName() {
		return getIdentifier().getDslContainerName();
	}

	default String getQuarkusArtifactId() {
		return getIdentifier().getQuarkusArtifactId();
	}

	Map<?,?> getProperties();

	void property(Object key, Object value);
}
