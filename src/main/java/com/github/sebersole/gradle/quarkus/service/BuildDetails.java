package com.github.sebersole.gradle.quarkus.service;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.dsl.NativeArguments;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

public class BuildDetails {
	private final Property<String> quarkusVersionProperty;
	private final Services services;
	private final DirectoryProperty workingDirectoryProperty;
	private final Provider<NativeArguments> nativeArgumentsProvider;

	private final Project mainProject;

	private final Configuration platforms;
	private final Configuration runtimeDependencies;
	private final Configuration deploymentDependencies;


	@SuppressWarnings( "UnstableApiUsage" )
	public BuildDetails(Project mainProject, Services services) {
		this.services = services;

		quarkusVersionProperty = mainProject.getObjects().property( String.class );
		quarkusVersionProperty.convention( "1.7.1.Final" );

		workingDirectoryProperty = mainProject.getObjects().directoryProperty();
		workingDirectoryProperty.convention( mainProject.getLayout().getBuildDirectory().dir( QUARKUS ) );

		final NativeArguments nativeArguments = new NativeArguments();
		nativeArgumentsProvider = mainProject.provider( () -> nativeArguments );

		this.mainProject = mainProject;

		this.platforms = mainProject.getConfigurations().maybeCreate( "quarkusPlatforms" );
		this.platforms.setDescription( "Configuration to specify all Quarkus platforms (BOMs) to be applied" );

		this.runtimeDependencies = mainProject.getConfigurations().maybeCreate( "quarkusRuntime" );
		this.runtimeDependencies.setDescription( "Collective runtime dependencies for all applied Quarkus extensions" );
		this.runtimeDependencies.extendsFrom( platforms );

		this.deploymentDependencies = mainProject.getConfigurations().create( "quarkusDeployment" );
		this.deploymentDependencies.setDescription( "Collective deployment dependencies for all applied Quarkus extensions" );
		this.deploymentDependencies.extendsFrom( platforms );

		final DependencyHandler dependencyHandler = mainProject.getDependencies();
		dependencyHandler.add(
				platforms.getName(),
				dependencyHandler.enforcedPlatform(
						Helper.groupArtifactVersion(
								Helper.QUARKUS_GROUP,
								Helper.QUARKUS_BOM,
								quarkusVersionProperty.get()
						)
				)
		);
	}

	public Services getServices() {
		return services;
	}

	public String getQuarkusVersion() {
		return quarkusVersionProperty.get();
	}

	public Directory getQuarkusWorkingDirectory() {
		return workingDirectoryProperty.get();
	}

	public Property<String> getQuarkusVersionProperty() {
		return quarkusVersionProperty;
	}

	public DirectoryProperty getWorkingDirectoryProperty() {
		return workingDirectoryProperty;
	}

	public Provider<NativeArguments> getNativeArgumentsProvider() {
		return nativeArgumentsProvider;
	}

	public Configuration getPlatforms() {
		return platforms;
	}

	public Dependency addPlatform(Object notation) {
		return mainProject.getDependencies().add( platforms.getName(), notation );
	}

	public Configuration getRuntimeDependencies() {
		return runtimeDependencies;
	}

	public Configuration getDeploymentDependencies() {
		return deploymentDependencies;
	}

	public Project getMainProject() {
		return mainProject;
	}
}
