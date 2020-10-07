package com.github.sebersole.gradle.quarkus.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusConfigException;
import com.github.sebersole.gradle.quarkus.dsl.NativeArguments;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

public class BuildDetails {
	private final Property<String> quarkusVersionProperty;
	private final Services services;
	private final DirectoryProperty workingDirectoryProperty;
	private final Provider<NativeArguments> nativeArgumentsProvider;

	private final Project mainProject;
	private final ProjectInfo mainProjectInfo;
	private final Properties applicationProperties;

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
		this.mainProjectInfo = new ProjectInfo(
				mainProject.getPath(),
				mainProject.getGroup().toString(),
				mainProject.getName(),
				mainProject.getVersion().toString(),
				mainProject.getLayout().getProjectDirectory(),
				mainProject.getConvention().getPlugin( JavaPluginConvention.class )
		);
		this.applicationProperties = loadApplicationProperties( mainProjectInfo, services );

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

	private static Properties loadApplicationProperties(ProjectInfo mainProjectInfo, Services services) {
		final Properties applicationProperties = new Properties();

		// cheat a little and look at the source files
		//    - this avoids an undesirable chicken-egg problem

		final Set<File> resourceSrcDirs = mainProjectInfo.getMainSourceSet().getResources().getSrcDirs();
		for ( File resourceSrcDir : resourceSrcDirs ) {
			final File propFile = new File( new File( resourceSrcDir, "META-INF" ), "application.properties" );
			if ( propFile.exists() ) {
				try ( final FileInputStream stream = new FileInputStream( propFile ) ) {
					applicationProperties.load( stream );
					// use just the first...
					break;
				}
				catch (Exception e) {
					throw new QuarkusConfigException( "Unable to access `application.properties`" );
				}
			}
		}

		return applicationProperties;
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

	public ProjectInfo getMainProjectInfo() {
		return mainProjectInfo;
	}

	public String getApplicationProperty(String name) {
		return applicationProperties.getProperty( name );
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
