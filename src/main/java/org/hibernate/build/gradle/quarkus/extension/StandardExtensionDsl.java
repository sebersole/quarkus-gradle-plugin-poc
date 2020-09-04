package org.hibernate.build.gradle.quarkus.extension;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.artifacts.Configuration;

import org.hibernate.build.gradle.quarkus.Helper;
import org.hibernate.build.gradle.quarkus.QuarkusDsl;

/**
 * Standard DSL implementation for configuring Quarkus extensions
 *
 * @author Steve Ebersole
 */
public class StandardExtensionDsl implements ExtensionDslImplementor, Serializable {
	private final ExtensionIdentifier identifier;

	private final Map<Object,Object> properties = new HashMap<>();

	private final Configuration configuration;

	public StandardExtensionDsl(ExtensionIdentifier identifier, Configuration dependencyConfiguration, QuarkusDsl quarkusDsl) {
		this.identifier = identifier;

		assert ! identifier.getDslContainerName().startsWith( "quarkus-" );
		assert identifier.getQuarkusArtifactId().startsWith( "quarkus-" );
		assert ! identifier.getCamelCaseName().contains( "-" );

		this.configuration = dependencyConfiguration;

		// add the extension being configured as a dependency
		quarkusDsl.getProject().getDependencies().add(
				configuration.getName(),
				Helper.quarkusExtensionCoordinates(  identifier, quarkusDsl )
		);
	}

	@Override
	public ExtensionIdentifier getIdentifier() {
		return identifier;
	}

	public Map<?, ?> getProperties() {
		return properties;
	}

	public void property(Object key, Object value) {
		applyProperty( key, value );
	}

	protected <K,V> V applyProperty(K key, V value) {
		//noinspection unchecked
		return (V) properties.put( key, value );
	}

	@Override
	public Configuration getDependencyConfiguration() {
		return configuration;
	}
}
