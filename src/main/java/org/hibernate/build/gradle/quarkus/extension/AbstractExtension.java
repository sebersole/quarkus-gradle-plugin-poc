package org.hibernate.build.gradle.quarkus.extension;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import org.hibernate.build.gradle.quarkus.Helper;
import org.hibernate.build.gradle.quarkus.QuarkusDsl;

import groovy.lang.Closure;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractExtension implements Extension, Serializable {
	private final QuarkusDsl quarkusDsl;
	private final String dslContainerName;
	private final String camelCaseName;

	private final Map<Object,Object> properties = new HashMap<>();
	private final Artifact artifact;

	private final Configuration dependencies;

	@FunctionalInterface
	protected interface ArtifactCreator {
		Artifact create(Extension extension, QuarkusDsl quarkusDsl);
	}

	public AbstractExtension(
			String dslContainerName,
			ArtifactCreator artifactCreator,
			QuarkusDsl quarkusDsl) {
		this(
				dslContainerName,
				Helper.extractCamelCaseName( dslContainerName ),
				artifactCreator,
				quarkusDsl
		);
	}

	public AbstractExtension(
			String dslContainerName,
			String camelCaseName,
			ArtifactCreator artifactCreator,
			QuarkusDsl quarkusDsl) {
		this.quarkusDsl = quarkusDsl;
		this.dslContainerName = dslContainerName;
		this.camelCaseName = camelCaseName;

		// Create a dependency Configuration specific to the extension
		this.dependencies = quarkusDsl.getProject().getConfigurations().maybeCreate( getCamelCaseName() );
		this.dependencies.setDescription( "Dependencies for the `" + dslContainerName + "` Quarkus extension" );

		this.artifact = artifactCreator.create( this, quarkusDsl );
	}

	@Override
	public String getDslContainerName() {
		return dslContainerName;
	}

	@Override
	public String getCamelCaseName() {
		return camelCaseName;
	}

	@Override
	public Artifact getArtifact() {
		return artifact;
	}

	@Override
	public Artifact artifact(Object notation) {
		return this.artifact( notation, null );
	}

	@Override
	public Artifact artifact(Object notation, Closure<Artifact> artifactClosure) {
		artifact.apply( notation, artifactClosure );
		return artifact;
	}

	@Override
	public Artifact quarkusArtifact(String artifactId) {
		return quarkusArtifact( artifactId, null );
	}

	@Override
	public Artifact quarkusArtifact(String artifactId, Closure<Artifact> artifactClosure) {
		assert artifact != null;
		artifact.apply(
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						Helper.QUARKUS + "-" + artifactId,
						quarkusDsl.getQuarkusVersion()
				),
				artifactClosure
		);
		return artifact;
	}

	@Override
	public Configuration getDependencies() {
		return dependencies;
	}

	@Override
	public Dependency dependency(Object notation) {
		return quarkusDsl.getProject().getDependencies().create( notation );
	}

	@Override
	public Dependency dependency(Object notation, Closure<Dependency> closure) {
		return quarkusDsl.getProject().getDependencies().create( notation, closure );
	}

	@Override
	public Map<?, ?> getProperties() {
		return properties;
	}

	@Override
	public void property(Object key, Object value) {
		applyProperty( key, value );
	}

	protected <K,V> V applyProperty(K key, V value) {
		//noinspection unchecked
		return (V) properties.put( key, value );
	}
}
