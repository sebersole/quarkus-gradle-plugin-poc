package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.extension.ExtensionCreationShortCuts;
import com.github.sebersole.gradle.quarkus.extension.ExtensionCreator;
import com.github.sebersole.gradle.quarkus.extension.HibernateOrmExtension;
import groovy.lang.Closure;

/**
 * Gradle DSL extension for configuring the plugin
 *
 * @author Steve Ebersole
 */
public class QuarkusDsl implements ExtensionCreationShortCuts, Serializable {
	private final Project project;
	private final ExtensionCreator extensionCreator;

	private String quarkusVersion = "1.7.1.Final";
	private File workingDir = new File( "/tmp" );

	private String testProfile = "prod";

	private NativeArguments nativeArgs;

	private final NamedDomainObjectContainer<Extension> extensions;
	private final Map<String, Extension> extensionsByGav = new HashMap<>();

	private final Configuration runtimeConfiguration;
	private final Configuration quarkusPlatforms;

	public QuarkusDsl(Project project) {
		this.project = project;

		quarkusPlatforms = project.getConfigurations().maybeCreate( "quarkusPlatforms" );
		quarkusPlatforms.setDescription( "Configuration to specify all Quarkus platforms (BOMs) to be applied" );

		// Apply the standard BOM
		project.getDependencies().add(
				quarkusPlatforms.getName(),
				project.getDependencies().enforcedPlatform( Helper.groupArtifactVersion( Helper.QUARKUS_GROUP, Helper.QUARKUS_BOM, getQuarkusVersion() ) )
		);
		// todo : apply QUARKUS_UNIVERSE_COMMUNITY_BOM also?
		//		- for now see the sample test project

		this.runtimeConfiguration = project.getConfigurations().create( "quarkusRuntime" );
		this.runtimeConfiguration.extendsFrom( quarkusPlatforms );
		this.runtimeConfiguration.setDescription( "Collective dependencies for all applied Quarkus extensions" );

		this.extensionCreator = new ExtensionCreator( this, quarkusPlatforms );
		this.extensions = project.container( Extension.class, extensionCreator );
	}

	public Project getProject() {
		return project;
	}

	public String getQuarkusVersion() {
		return quarkusVersion;
	}

	public void setQuarkusVersion(String quarkusVersion) {
		this.quarkusVersion = quarkusVersion;
	}

	public void quarkusVersion(String quarkusVersion) {
		setQuarkusVersion( quarkusVersion );
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(Object workingDir) {
		this.workingDir = project.file( workingDir );
	}

	public void workingDir(Object workingDir) {
		setWorkingDir( workingDir );
	}

	public String getTestProfile() {
		return testProfile;
	}

	public void setTestProfile(String testProfile) {
		this.testProfile = testProfile;
	}

	public void testProfile(String testProfile) {
		setTestProfile( testProfile );
	}

	public NativeArguments getNativeArgs() {
		if ( nativeArgs == null ) {
			nativeArgs = new NativeArguments( this );
		}
		return nativeArgs;
	}

	public void setNativeArgs(NativeArguments nativeArgs) {
		this.nativeArgs = nativeArgs;
	}

	public void nativeArgs(NativeArguments nativeArgs) {
		setNativeArgs( nativeArgs );
	}

	public void nativeArgs(Closure<NativeArguments> configurer) {
		ConfigureUtil.configure( configurer, getNativeArgs() );
	}

	public void nativeArgs(Action<NativeArguments> configurer) {
		configurer.execute( getNativeArgs() );
	}

	public void platform(String gav) {
		final DependencyHandler dependencyHandler = project.getDependencies();
		dependencyHandler.add(
				quarkusPlatforms.getName(),
				dependencyHandler.enforcedPlatform( gav )
		);
	}

	public void platform(String gav, Closure<Dependency> closure) {
		final DependencyHandler dependencyHandler = project.getDependencies();
		dependencyHandler.add(
				quarkusPlatforms.getName(),
				dependencyHandler.enforcedPlatform( gav ),
				closure
		);
	}

	public void platform(String gav, Action<Dependency> action) {
		final DependencyHandler dependencyHandler = project.getDependencies();
		final Dependency dependency = dependencyHandler.add(
				quarkusPlatforms.getName(),
				dependencyHandler.enforcedPlatform( gav )
		);
		action.execute( dependency );
	}

	public NamedDomainObjectContainer<Extension> getModules() {
		return extensions;
	}

	public void modules(Closure<Extension> extensionClosure) {
		ConfigureUtil.configure( extensionClosure, extensions );
	}

	public void extensions(Closure<Extension> extensionClosure) {
		ConfigureUtil.configure( extensionClosure, extensions );
	}

	public Configuration getRuntimeConfiguration() {
		return runtimeConfiguration;
	}

	public Extension findExtensionByGav(String gav) {
		return extensionsByGav.get( gav );
	}

	public void registerExtensionByGav(String gav, Extension extension) {
		final Extension existing = extensionsByGav.put( gav, extension );
		assert existing == null;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// ExtensionCreationShortCuts
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


	@Override
	public QuarkusDsl getQuarkusDsl() {
		return this;
	}

	@Override
	public BiConsumer<Extension, QuarkusDsl> getCreatedExtensionPreparer() {
		return extensionCreator::prepareExtension;
	}
}
