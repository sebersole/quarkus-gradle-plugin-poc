package org.hibernate.build.gradle.quarkus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import org.hibernate.build.gradle.quarkus.extension.ExtensionDslCreator;
import org.hibernate.build.gradle.quarkus.extension.ExtensionDsl;
import org.hibernate.build.gradle.quarkus.extension.ExtensionDslImplementor;
import org.hibernate.build.gradle.quarkus.extension.ExtensionIdentifier;

/**
 * Gradle DSL extension for configuring the plugin
 *
 * @author Steve Ebersole
 */
public class QuarkusDsl implements Serializable {
	private final Project project;

	private String quarkusLevelConfig;

	private final NamedDomainObjectContainer<ExtensionDslImplementor> modules;
	private final Configuration runtimeConfiguration;

	private final Map<String, ExtensionIdentifier> extensionIdentifierMap = new HashMap<>();
	private final Map<String, String> versionBomMap;


	public QuarkusDsl(Project project) {
		this.project = project;

		this.modules = project.container(
				ExtensionDslImplementor.class,
				new ExtensionDslCreator( this )
		);

		this.runtimeConfiguration = project.getConfigurations().create( "quarkusRuntime" );

		this.versionBomMap = loadVersionBomMap();
	}

	private Map<String, String> loadVersionBomMap() {
		final HashMap<String, String> map = new HashMap<>();

		map.put( "com.github.ben-manes.caffeine:caffeine", "2.8.5" );

		return map;
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

	public Map<String, String> getVersionBomMap() {
		return versionBomMap;
	}

	public void mapVersion(String moduleName, String version) {
		versionBomMap.put( moduleName, version );
	}
}
