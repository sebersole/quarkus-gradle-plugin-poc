package org.hibernate.build.gradle.quarkus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import org.hibernate.build.gradle.quarkus.extension.ExtensionDsl;
import org.hibernate.build.gradle.quarkus.extension.ExtensionDslCreator;
import org.hibernate.build.gradle.quarkus.extension.ExtensionDslImplementor;
import org.hibernate.build.gradle.quarkus.extension.ExtensionIdentifier;
import org.hibernate.build.gradle.quarkus.extension.ExtensionModuleCreationListener;

import static org.hibernate.build.gradle.quarkus.Helper.QUARKUS;

/**
 * Gradle DSL extension for configuring the plugin
 *
 * @author Steve Ebersole
 */
public class QuarkusDsl implements Serializable {
	private final Project project;

	private String quarkusLevelConfig;

	private final NamedDomainObjectContainer<ExtensionDslImplementor> modules;

	private final Configuration bomConfiguration;
	private final Configuration runtimeConfiguration;

	private final Map<String, ExtensionIdentifier> extensionIdentifierMap = new HashMap<>();

	public QuarkusDsl(Project project, ExtensionModuleCreationListener extensionListener) {
		this.project = project;

		final DependencyHandler dependencyHandler = project.getDependencies();
		final String bomGav = Helper.groupArtifactVersion( Helper.QUARKUS_GROUP, Helper.QUARKUS_BOM, getQuarkusVersion() );
		final Dependency bomDependency = dependencyHandler.enforcedPlatform( bomGav );

		this.bomConfiguration = project.getConfigurations().create( "quarkusBom" );
		dependencyHandler.add( bomConfiguration.getName(), bomDependency );

		this.runtimeConfiguration = project.getConfigurations().create( "quarkusRuntime" );
		this.runtimeConfiguration.extendsFrom( bomConfiguration );

		this.modules = project.container(
				ExtensionDslImplementor.class,
				new ExtensionDslCreator(
						this,
						extensionListener,
						bomConfiguration
				)
		);

	}

	public Project getProject() {
		return project;
	}

	public String getQuarkusVersion() {
		// todo : discuss ways to handle this
		//		- settable?
		//		- injectable?
		//		- inherent?
		return "1.7.1.Final";
	}

	public ExtensionIdentifier resolveExtensionIdentifier(String containerName, Supplier<ExtensionIdentifier> creator) {
		return extensionIdentifierMap.computeIfAbsent( containerName, s -> creator.get() );
	}

	public Configuration getRuntimeConfiguration() {
		return runtimeConfiguration;
	}

	public String getQuarkusLevelConfig() {
		return quarkusLevelConfig;
	}

	public void setQuarkusLevelConfig(String quarkusLevelConfig) {
		this.quarkusLevelConfig = quarkusLevelConfig;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public NamedDomainObjectContainer<ExtensionDsl> getModules() {
		return (NamedDomainObjectContainer) modules;
	}
}
