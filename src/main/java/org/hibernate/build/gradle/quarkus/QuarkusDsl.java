package org.hibernate.build.gradle.quarkus;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.util.ConfigureUtil;

import org.hibernate.build.gradle.quarkus.extension.ExtensionDsl;
import org.hibernate.build.gradle.quarkus.extension.ExtensionDslCreator;
import org.hibernate.build.gradle.quarkus.extension.ExtensionDslImplementor;
import org.hibernate.build.gradle.quarkus.extension.ExtensionIdentifier;
import org.hibernate.build.gradle.quarkus.extension.ExtensionModuleCreationListener;

import groovy.lang.Closure;

/**
 * Gradle DSL extension for configuring the plugin
 *
 * @author Steve Ebersole
 */
public class QuarkusDsl implements Serializable {
	private final Project project;

	private String quarkusVersion = "1.7.1.Final";
	private File workingDir = new File( "/tmp" );

	private String testProfile = "prod";

	private NativeArguments nativeArgs;

	private final Configuration runtimeConfiguration;

	private final NamedDomainObjectContainer<ExtensionDslImplementor> modules;
	private final Map<String, ExtensionIdentifier> extensionIdentifierMap = new HashMap<>();

	public QuarkusDsl(Project project, ExtensionModuleCreationListener extensionListener) {
		this.project = project;

		final DependencyHandler dependencyHandler = project.getDependencies();

		final String bomGav = Helper.groupArtifactVersion( Helper.QUARKUS_GROUP, Helper.QUARKUS_BOM, getQuarkusVersion() );
		final Configuration bomConfiguration = project.getConfigurations().detachedConfiguration( dependencyHandler.enforcedPlatform( bomGav ) );

		this.runtimeConfiguration = project.getConfigurations().create( "quarkusRuntime" );
		this.runtimeConfiguration.extendsFrom( bomConfiguration );
		this.runtimeConfiguration.setDescription( "Collective dependencies for all applied Quarkus extensions" );

		this.modules = project.container(
				ExtensionDslImplementor.class,
				new ExtensionDslCreator( this, extensionListener, bomConfiguration )
		);
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

	public Configuration getRuntimeConfiguration() {
		return runtimeConfiguration;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public NamedDomainObjectContainer<ExtensionDsl> getModules() {
		return (NamedDomainObjectContainer) modules;
	}

	public ExtensionIdentifier resolveExtensionIdentifier(String containerName, Supplier<ExtensionIdentifier> creator) {
		return extensionIdentifierMap.computeIfAbsent( containerName, s -> creator.get() );
	}
}
