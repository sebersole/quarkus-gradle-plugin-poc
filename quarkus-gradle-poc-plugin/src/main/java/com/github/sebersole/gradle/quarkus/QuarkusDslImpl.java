package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.extension.ExtensionFactorySupport;
import com.github.sebersole.gradle.quarkus.extension.StandardExtension;
import com.github.sebersole.gradle.quarkus.extension.orm.HibernateOrmExtension;
import com.github.sebersole.gradle.quarkus.extension.orm.HibernateOrmExtensionFactory;
import groovy.lang.Closure;

/**
 * Gradle DSL extension for configuring the plugin
 *
 * @author Steve Ebersole
 */
public class QuarkusDslImpl extends AbstractExtensionCreationShortCuts implements QuarkusDsl {
	private final Project project;

	private String quarkusVersion = "1.7.1.Final";
	private File workingDir = new File( "/tmp" );

	private String testProfile = "prod";

	private NativeArguments nativeArgs;

	private final PolymorphicDomainObjectContainer<Extension> extensions;

	private final Configuration quarkusPlatforms;

	private final Configuration runtimeDependencies;
	private final Configuration deploymentDependencies;

	private final BuildState buildState = new BuildState();

	public QuarkusDslImpl(Project project) {
		this.project = project;

		quarkusPlatforms = project.getConfigurations().maybeCreate( "quarkusPlatforms" );
		quarkusPlatforms.setDescription( "Configuration to specify all Quarkus platforms (BOMs) to be applied" );

		// Apply the standard BOM
		project.getDependencies().add(
				quarkusPlatforms.getName(),
				project.getDependencies().enforcedPlatform( Helper.groupArtifactVersion( Helper.QUARKUS_GROUP, Helper.QUARKUS_BOM, getQuarkusVersion() ) )
		);
		// todo : apply QUARKUS_UNIVERSE_COMMUNITY_BOM also?

		this.runtimeDependencies = project.getConfigurations().create( "quarkusRuntime" );
		this.runtimeDependencies.extendsFrom( quarkusPlatforms );
		this.runtimeDependencies.setDescription( "Collective runtime dependencies for all applied Quarkus extensions" );

		this.deploymentDependencies = project.getConfigurations().create( "quarkusDeployment" );
		this.deploymentDependencies.extendsFrom( quarkusPlatforms );
		this.deploymentDependencies.setDescription( "Collective deployment dependencies for all applied Quarkus extensions" );

		this.extensions = prepareExtensionsContainer( project );
	}

	public PolymorphicDomainObjectContainer<Extension> prepareExtensionsContainer(Project project) {
		final ExtensiblePolymorphicDomainObjectContainer<Extension> extensions = project.getObjects().polymorphicDomainObjectContainer( Extension.class );

		final HibernateOrmExtensionFactory hibernateOrmExtensionFactory = new HibernateOrmExtensionFactory( this );
		extensions.registerFactory( HibernateOrmExtension.class, hibernateOrmExtensionFactory );

		// default factory
		extensions.registerFactory(
				Extension.class,
				name -> {
					// hackalicious
					if ( HibernateOrmExtension.CONTAINER_NAME.equals( name )
							|| HibernateOrmExtension.ARTIFACT_SHORT_NAME.equals( name ) ) {
						return hibernateOrmExtensionFactory.create( name );
					}

					final StandardExtension extension = new StandardExtension( name, this );
					ExtensionFactorySupport.prepareExtension( extension, this );
					return extension;
				}
		);

		return extensions;
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public String getQuarkusVersion() {
		return quarkusVersion;
	}

	public File getWorkingDirectory() {
		return getWorkingDir();
	}

	public Configuration getPlatforms() {
		return quarkusPlatforms;
	}

	@Override
	public void setQuarkusVersion(String quarkusVersion) {
		this.quarkusVersion = quarkusVersion;
	}

	@Override
	public void quarkusVersion(String quarkusVersion) {
		setQuarkusVersion( quarkusVersion );
	}

	@Override
	public File getWorkingDir() {
		return workingDir;
	}

	@Override
	public void setWorkingDir(Object workingDir) {
		this.workingDir = project.file( workingDir );
	}

	@Override
	public void workingDir(Object workingDir) {
		setWorkingDir( workingDir );
	}

	@Override
	public String getTestProfile() {
		return testProfile;
	}

	@Override
	public void setTestProfile(String testProfile) {
		this.testProfile = testProfile;
	}

	@Override
	public void testProfile(String testProfile) {
		setTestProfile( testProfile );
	}

	@Override
	public NativeArguments getNativeArgs() {
		if ( nativeArgs == null ) {
			nativeArgs = new NativeArguments();
		}
		return nativeArgs;
	}

	public void setNativeArgs(NativeArguments nativeArgs) {
		this.nativeArgs = nativeArgs;
	}

	public void nativeArgs(NativeArguments nativeArgs) {
		setNativeArgs( nativeArgs );
	}

	@Override
	public void nativeArgs(Closure<NativeArguments> configurer) {
		ConfigureUtil.configure( configurer, getNativeArgs() );
	}

	@Override
	public void nativeArgs(Action<NativeArguments> configurer) {
		configurer.execute( getNativeArgs() );
	}

	@Override
	public void platform(String gav) {
		final DependencyHandler dependencyHandler = project.getDependencies();
		dependencyHandler.add(
				quarkusPlatforms.getName(),
				dependencyHandler.enforcedPlatform( gav )
		);
	}

	@Override
	public void platform(String gav, Closure<Dependency> closure) {
		final DependencyHandler dependencyHandler = project.getDependencies();
		dependencyHandler.add(
				quarkusPlatforms.getName(),
				dependencyHandler.enforcedPlatform( gav ),
				closure
		);
	}

	@Override
	public void platform(String gav, Action<Dependency> action) {
		final DependencyHandler dependencyHandler = project.getDependencies();
		final Dependency dependency = dependencyHandler.add(
				quarkusPlatforms.getName(),
				dependencyHandler.enforcedPlatform( gav )
		);
		action.execute( dependency );
	}

	@Override
	public void extensions(Closure<PolymorphicDomainObjectContainer<Extension>> extensionClosure) {
		ConfigureUtil.configure( extensionClosure, extensions );
	}

	@Override
	public void extensions(Action<PolymorphicDomainObjectContainer<Extension>> action) {
		action.execute( extensions );
	}

	@Override
	public void quarkusExtensions(Closure<PolymorphicDomainObjectContainer<Extension>> extensionClosure) {
		ConfigureUtil.configure( extensionClosure, extensions );
	}

	public PolymorphicDomainObjectContainer<Extension> getQuarkusExtensions() {
		return extensions;
	}

	@Override
	public void visitExtensions(Consumer<Extension> consumer) {
		extensions.forEach( consumer );
	}

	@Override
	public Configuration getRuntimeDependencies() {
		return runtimeDependencies;
	}

	@Override
	public Configuration getDeploymentDependencies() {
		return deploymentDependencies;
	}

	public BuildState getBuildState() {
		return buildState;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// ExtensionCreationShortCuts
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public QuarkusDslImpl getQuarkusDsl() {
		return this;
	}

	@Override
	public BiConsumer<Extension, QuarkusDsl> getCreatedExtensionPreparer() {
		return (extension, quarkusDsl) -> ExtensionFactorySupport.prepareExtension( extension, this );
	}
}
