package com.github.sebersole.gradle.quarkus.extension;

import java.io.Serializable;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;

import groovy.lang.Closure;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractExtension implements Extension, Serializable {
	private final QuarkusDsl quarkusDsl;

	private final String dslContainerName;
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
		this.quarkusDsl = quarkusDsl;
		this.dslContainerName = dslContainerName;

		// Create a dependency Configuration specific to the extension
		this.dependencies = quarkusDsl.getProject().getConfigurations().maybeCreate( dslContainerName );
		this.dependencies.setDescription( "Dependencies for the `" + dslContainerName + "` Quarkus extension" );

		this.artifact = artifactCreator.create( this, quarkusDsl );
	}

	@Override
	public String getDslName() {
		return dslContainerName;
	}

	@Override
	public Artifact getArtifact() {
		return artifact;
	}

	@Override
	public Artifact artifact(Action<Artifact> artifactAction) {
		artifactAction.execute( artifact );
		return artifact;
	}

	@Override
	public Artifact artifact(Object notation) {
		artifact.setDependency( notation );
		return artifact;
	}

	@Override
	public Artifact artifact(Object notation, Closure<Artifact> artifactClosure) {
		artifact.setDependency( notation );
		ConfigureUtil.configure( artifactClosure, artifact );
		return artifact;
	}

	@Override
	public Artifact artifact(Object notation, Action<Artifact> artifactAction) {
		artifact.setDependency( notation );
		artifactAction.execute( artifact );
		return artifact;
	}

	@Override
	public Artifact quarkusArtifact(String shortName) {
		artifact.setDependency( quarkusArtifactId( shortName, quarkusDsl ) );
		return artifact;
	}

	private static String quarkusArtifactId(String shortName, QuarkusDsl quarkusDsl) {
		assert shortName != null;
		return Helper.groupArtifactVersion(
				Helper.QUARKUS_GROUP,
				Helper.QUARKUS + "-" + shortName,
				quarkusDsl.getQuarkusVersion()
		);
	}

	@Override
	public Artifact quarkusArtifact(String shortName, Closure<Artifact> artifactClosure) {
		artifact.setDependency( quarkusArtifactId( shortName, quarkusDsl ) );
		ConfigureUtil.configure( artifactClosure, artifact );
		return artifact;
	}

	@Override
	public Artifact quarkusArtifact(String shortName, Action<Artifact> artifactAction) {
		assert shortName != null;

		artifact.setDependency( quarkusArtifactId( shortName, quarkusDsl ) );
		artifactAction.execute( artifact );
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
	public Dependency dependency(Object notation, Action<Dependency> action) {
		final Dependency dependency = quarkusDsl.getProject().getDependencies().create( notation );
		action.execute( dependency );
		return dependency;
	}
}
