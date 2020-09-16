package com.github.sebersole.gradle.quarkus.extension;

import java.io.Serializable;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.Artifact;
import com.github.sebersole.gradle.quarkus.Extension;
import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;

import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import groovy.lang.Closure;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractExtension implements Extension, Serializable {
	private final QuarkusDslImpl quarkusDsl;

	private final String dslContainerName;

	private final Artifact artifact;
	private final Artifact deploymentArtifact;

	private final Configuration runtimeDependencies;
	private final Configuration deploymentDependencies;

	@FunctionalInterface
	protected interface ArtifactCreator {
		Artifact create(Extension extension, QuarkusDslImpl quarkusDsl);
	}

	public AbstractExtension(
			String dslContainerName,
			ArtifactCreator artifactCreator,
			ArtifactCreator deploymentArtifactCreator,
			QuarkusDslImpl quarkusDsl) {
		this.dslContainerName = dslContainerName;
		this.quarkusDsl = quarkusDsl;

		this.runtimeDependencies = quarkusDsl.getProject().getConfigurations().maybeCreate( dslContainerName + "Runtime" );
		this.runtimeDependencies.setDescription( "Runtime dependencies for the `" + dslContainerName + "` Quarkus extension" );

		this.deploymentDependencies = quarkusDsl.getProject().getConfigurations().maybeCreate( dslContainerName + "Deployment" );
		this.deploymentDependencies.setDescription( "Deployment dependencies for the `" + dslContainerName + "` Quarkus extension" );

		this.artifact = artifactCreator.create( this, quarkusDsl );
		this.deploymentArtifact = deploymentArtifactCreator.create( this, quarkusDsl );
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
	public void artifact(Action<Artifact> artifactAction) {
		artifactAction.execute( artifact );
	}

	@Override
	public Artifact artifact(Object notation) {
		artifact.setDependency( notation );
		return artifact;
	}

	@Override
	public void artifact(Object notation, Closure<Artifact> artifactClosure) {
		artifact.setDependency( notation );
		ConfigureUtil.configure( artifactClosure, artifact );
	}

	@Override
	public void artifact(Object notation, Action<Artifact> artifactAction) {
		artifact.setDependency( notation );
		artifactAction.execute( artifact );
	}

	@Override
	public Artifact getDeploymentArtifact() {
		return deploymentArtifact;
	}

	@Override
	public void deploymentArtifact(Action<Artifact> action) {
		action.execute( deploymentArtifact );
	}

	@Override
	public Artifact deploymentArtifact(Object notation) {
		deploymentArtifact.setDependency( notation );
		return deploymentArtifact;
	}

	@Override
	public void deploymentArtifact(Object notation, Closure<Artifact> artifactClosure) {
		deploymentArtifact.setDependency( notation );
		ConfigureUtil.configure( artifactClosure, deploymentArtifact );
	}

	@Override
	public void deploymentArtifact(Object notation, Action<Artifact> artifactAction) {
		deploymentArtifact.setDependency( notation );
		artifactAction.execute( deploymentArtifact );
	}

	@Override
	public Artifact quarkusArtifact(String shortName) {
		artifact.setDependency( quarkusArtifactId( shortName, quarkusDsl ) );
		deploymentArtifact.setDependency( quarkusDeploymentArtifactId( shortName, quarkusDsl ) );
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

	private static String quarkusDeploymentArtifactId(String shortName, QuarkusDsl quarkusDsl) {
		assert shortName != null;
		return Helper.groupArtifactVersion(
				Helper.QUARKUS_GROUP,
				Helper.QUARKUS + "-" + shortName + "-deployment",
				quarkusDsl.getQuarkusVersion()
		);
	}

	@Override
	public void quarkusArtifact(String shortName, Closure<Artifact> artifactClosure) {
		artifact.setDependency( quarkusArtifactId( shortName, quarkusDsl ) );
		deploymentArtifact.setDependency( quarkusDeploymentArtifactId( shortName, quarkusDsl ) );
		ConfigureUtil.configure( artifactClosure, artifact );
	}

	@Override
	public void quarkusArtifact(String shortName, Action<Artifact> artifactAction) {
		assert shortName != null;

		artifact.setDependency( quarkusArtifactId( shortName, quarkusDsl ) );
		deploymentArtifact.setDependency( quarkusDeploymentArtifactId( shortName, quarkusDsl ) );

		artifactAction.execute( artifact );
	}

	@Override
	public Configuration getRuntimeDependencies() {
		return runtimeDependencies;
	}

	@Override
	public Dependency runtimeDependency(Object notation) {
		return quarkusDsl.getProject().getDependencies().add( runtimeDependencies.getName(), notation );
	}

	@Override
	public void runtimeDependency(Object notation, Closure<Dependency> closure) {
		quarkusDsl.getProject().getDependencies().add( runtimeDependencies.getName(), notation, closure );
	}

	@Override
	public void runtimeDependency(Object notation, Action<Dependency> action) {
		final Dependency dependency = runtimeDependency( notation );
		action.execute( dependency );
	}

	@Override
	public Configuration getDeploymentDependencies() {
		return deploymentDependencies;
	}

	@Override
	public Dependency deploymentDependency(Object notation) {
		return quarkusDsl.getProject().getDependencies().add( deploymentDependencies.getName(), notation );
	}

	@Override
	public void deploymentDependency(Object notation, Closure<Dependency> closure) {
		quarkusDsl.getProject().getDependencies().add( deploymentDependencies.getName(), notation, closure );
	}

	@Override
	public void deploymentDependency(Object notation, Action<Dependency> action) {
		final Dependency dependency = deploymentDependency( notation );
		action.execute( dependency );
	}
}
