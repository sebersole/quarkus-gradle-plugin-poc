package com.github.sebersole.gradle.quarkus.dsl;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;
import com.github.sebersole.gradle.quarkus.service.Services;
import groovy.lang.Closure;

/**
 * Gradle DSL extension for configuring the Quarkus build.
 */
@SuppressWarnings( { "unused", "RedundantSuppression" } )
public class QuarkusConfig {
	private final Project project;
	private final BuildDetails buildDetails;

	private final ExtensionsConfig extensionsConfig;

	public QuarkusConfig(Services services) {
		this.buildDetails = services.getBuildDetails();
		this.project = buildDetails.getMainProject();

		this.extensionsConfig = new ExtensionsConfig( buildDetails );
	}

	public Property<String> getQuarkusVersion() {
		return buildDetails.getQuarkusVersionProperty();
	}

	public DirectoryProperty getWorkingDirectory() {
		return buildDetails.getWorkingDirectoryProperty();
	}

	public void nativeArgs(Closure<NativeArguments> closure) {
		ConfigureUtil.configure( closure, buildDetails.getNativeArgumentsProvider().get() );
	}

	public void nativeArgs(Action<NativeArguments> action) {
		action.execute( buildDetails.getNativeArgumentsProvider().get() );
	}

	public void platforms(Closure closure) {
		// NOTE : PlatformsConfigGroovyDelegate adds support for specifying "well known" platforms.  i.e.
		//		platforms {
		//			quarkusUniverse()
		//		}

		closure.setDelegate( new PlatformsConfigGroovyDelegate( buildDetails ) );
		closure.setResolveStrategy( Closure.DELEGATE_FIRST );

		closure.call( buildDetails.getPlatforms() );
	}

	public void platforms(Action<Configuration> action) {
		// this assumes a call from Kotlin script, although it is certainly possible
		// to explicitly create an Action.
		action.execute( buildDetails.getPlatforms() );
	}

	@SuppressWarnings( "UnstableApiUsage" )
	public void platform(String gav) {
		final Configuration platforms = buildDetails.getPlatforms();

		final DependencyHandler dependencyHandler = project.getDependencies();
		dependencyHandler.add( platforms.getName(), dependencyHandler.enforcedPlatform( gav ) );
	}

	@SuppressWarnings( "UnstableApiUsage" )
	public void platform(String gav, Closure<Dependency> closure) {
		final Configuration platforms = buildDetails.getPlatforms();

		final DependencyHandler dependencyHandler = project.getDependencies();
		dependencyHandler.add( platforms.getName(), dependencyHandler.enforcedPlatform( gav ), closure );
	}

	@SuppressWarnings( "UnstableApiUsage" )
	public void platform(String gav, Action<Dependency> action) {
		final Configuration platforms = buildDetails.getPlatforms();

		final DependencyHandler dependencyHandler = project.getDependencies();
		final Dependency dependency = dependencyHandler.add( platforms.getName(), dependencyHandler.enforcedPlatform( gav ) );

		assert dependency != null;

		action.execute( dependency );
	}

	public ExtensionsConfig getExtensionsConfig() {
		return extensionsConfig;
	}

	public void extensions(Closure<ExtensionsConfig> extensionClosure) {
		ConfigureUtil.configure( extensionClosure, extensionsConfig );
	}

	public void extensions(Action<ExtensionsConfig> action) {
		action.execute( extensionsConfig );
	}

	public void quarkusExtensions(Closure<ExtensionsConfig> extensionClosure) {
		extensions( extensionClosure );
	}

	public void quarkusExtensions(Action<ExtensionsConfig> action) {
		extensions( action );
	}
}
