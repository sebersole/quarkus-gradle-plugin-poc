package org.hibernate.build.gradle.quarkus.extension;

import java.util.Map;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import groovy.lang.Closure;

/**
 * Basic contract for an "extension config" object
 *
 * @author Steve Ebersole
 */
public interface Extension extends Named {
	/**
	 * The name used for the Gradle {@link org.gradle.api.NamedDomainObjectContainer}.  This
	 * is the artifact-id minus the 'quarkus-' prefix (e.g. 'hibernate-orm' for
	 * 'quarkus-hibernate-orm')
	 */
	String getDslContainerName();

	@Override
	default String getName() {
		return getDslContainerName();
	}

	/**
	 * A camel-case version of the name that can be used in building various build components
	 */
	String getCamelCaseName();

	Artifact getArtifact();
	Artifact artifact(Object notation);
	Artifact artifact(Object notation, Closure<Artifact> artifactClosure);
	Artifact quarkusArtifact(String artifactId);
	Artifact quarkusArtifact(String artifactId, Closure<Artifact> artifactClosure);

	Configuration getDependencies();
	Dependency dependency(Object notation);
	Dependency dependency(Object notation, Closure<Dependency> closure);

	Map<?,?> getProperties();

	void property(Object key, Object value);
}
