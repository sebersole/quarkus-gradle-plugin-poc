package org.hibernate.build.gradle.quarkus;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import org.hibernate.build.gradle.quarkus.extension.ExtensionConfig;
import org.hibernate.build.gradle.quarkus.extension.HibernateOrmExtensionConfig;
import org.hibernate.build.gradle.quarkus.extension.StandardExtensionConfig;

/**
 * Gradle DSL extension for configuring the plugin
 *
 * @author Steve Ebersole
 */
public class QuarkusBuildConfig {
	private final QuarkusPlugin quarkusPlugin;
	private final Project project;

	private String quarkusLevelConfig;

	private final NamedDomainObjectContainer<ExtensionConfig> modules;

	private final Configuration deploymentConfiguration;
	private final Configuration runtimeConfiguration;

	public QuarkusBuildConfig(QuarkusPlugin quarkusPlugin, Project project) {
		this.quarkusPlugin = quarkusPlugin;
		this.project = project;

		this.deploymentConfiguration = project.getConfigurations().create( "quarkusDeployment" );
		this.runtimeConfiguration = project.getConfigurations().create( "quarkusRuntime" );

		this.modules = project.container(
				ExtensionConfig.class,
				new ExtensionConfigCreator( this )
		);
	}

	public String getQuarkusVersion() {
		// todo : discuss ways to handle this
		//		- settable?
		//		- injectable?
		//		- inherent?
		return "ThatOne";
	}

	public Project getProject() {
		return project;
	}

	public Configuration getDeploymentConfiguration() {
		return deploymentConfiguration;
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

	public NamedDomainObjectContainer<ExtensionConfig> getModules() {
		return modules;
	}
}
