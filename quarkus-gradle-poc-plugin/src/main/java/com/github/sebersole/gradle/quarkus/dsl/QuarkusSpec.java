package com.github.sebersole.gradle.quarkus.dsl;

import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;
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
public class QuarkusSpec {
	private final Project project;
	private final BuildDetails buildDetails;

	private final ExtensionSpecContainer extensionSpecs;
	private ExtensionsClosureDelegate extensionsClosureDelegate;

	public QuarkusSpec(Services services) {
		this.buildDetails = services.getBuildDetails();
		this.project = buildDetails.getMainProject();

		this.extensionSpecs = new ExtensionSpecContainer( buildDetails );
	}

	private ExtensionsClosureDelegate extensionsClosureDelegate() {
		if ( extensionsClosureDelegate == null ) {
			extensionsClosureDelegate = new ExtensionsClosureDelegate( buildDetails, extensionSpecs );
		}
		return extensionsClosureDelegate;
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

	public ExtensiblePolymorphicDomainObjectContainer<? extends ExtensionSpec> getExtensionSpecs() {
		return extensionSpecs.getContainer();
	}

	public void extensionSpecs(Action<ExtensiblePolymorphicDomainObjectContainer<? extends ExtensionSpec>> action) {
		action.execute( extensionSpecs.getContainer() );
	}

//	public void extensionSpecs(Closure<PolymorphicDomainObjectContainer<? extends ExtensionSpec>> extensionClosure) {
//		ConfigureUtil.configure( extensionClosure, extensionSpecs.getContainer() );
//	}

	/**
	 * Groovy DSL hook
	 */
	public void extensions(Closure<PolymorphicDomainObjectContainer<? extends ExtensionSpec>> extensionClosure) {
		extensionClosure.setDelegate( extensionsClosureDelegate() );
		extensionClosure.setResolveStrategy( Closure.DELEGATE_FIRST );
		extensionClosure.call( extensionSpecs.getContainer() );
//		ConfigureUtil.configure( extensionClosure, extensionSpecs.getContainer() );
	}

	/**
	 * Groovy DSL hook
	 */
	public void quarkusExtensions(Closure<PolymorphicDomainObjectContainer<? extends ExtensionSpec>> extensionClosure) {
		extensions( extensionClosure );
	}

	/**
	 * Kotlin DSL hook
	 */
	public void extensions(Action<ExtensionSpecContainer> action) {
		action.execute( extensionSpecs );
	}

}
