package org.hibernate.build.gradle.quarkus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Usage;

import org.hibernate.build.gradle.quarkus.extension.ExtensionDsl;

/**
 * Gradle DSL extension for configuring the plugin
 *
 * @author Steve Ebersole
 */
public class QuarkusDsl implements Serializable {
	private final Project project;

	private String quarkusLevelConfig;

	private final NamedDomainObjectContainer<ExtensionDsl> modules;

	private final Configuration deploymentConfiguration;
	private final Configuration runtimeConfiguration;

	private final Usage JAVA_API_USAGE;
	private final Bundling EXTERNAL_DEP_BUNDLING;


	public QuarkusDsl(Project project) {
		this.project = project;

		// ugh, see the note below
		this.JAVA_API_USAGE = project.getObjects().named( Usage.class, Usage.JAVA_API );
		this.EXTERNAL_DEP_BUNDLING = project.getObjects().named( Bundling.class, Bundling.EXTERNAL );

		this.deploymentConfiguration = project.getConfigurations().create( "quarkusDeployment" );
		configureDeploymentConfiguration( deploymentConfiguration, loadDependencyVersionMap(), project );

		this.runtimeConfiguration = project.getConfigurations().create( "quarkusRuntime" );

		this.modules = project.container(
				ExtensionDsl.class,
				new ExtensionConfigCreator( this )
		);
	}

	private void configureDeploymentConfiguration(
			Configuration configuration,
			Map<String, String> dependencyVersionMap,
			Project project) {
		// If the dependency is a Quarkus module, be sure to add it as an applied extension
		configuration.resolutionStrategy(
				resolutionStrategy -> resolutionStrategy.eachDependency(
						dependencyDetails -> {
							final ModuleVersionSelector requestedDependencyInfo = dependencyDetails.getRequested();
							if ( "io.quarkus".equals( requestedDependencyInfo.getGroup() ) ) {

								// For quarkus artifacts we want to do 2 things:
								//		1) make sure the extension is registered with the plugin (`QuarkusBuildConfig#modules`)
								//		2) force the same version to be used.

//								final String artifactId = requestedDependencyInfo.getName();
//								assert artifactId.startsWith( "quarkus-" );
//
//								final String containerName = Helper.containerName( artifactId );
//								final ExtensionDsl byName = modules.findByName( containerName );
//								if ( byName != null ) {
//									project.getLogger().debug( "Quarkus extension registration `{}` already existed : {}", containerName, byName );
//
//								}
//								else {
//									final ExtensionDsl created = modules.create( containerName );
//									project.getLogger().debug( "Quarkus extension registered `{}` : {}", containerName, created );
//								}


								// Force all Quarkus module versions to be the same
								dependencyDetails.useVersion( getQuarkusVersion() );
							}
						}
				)
		);

		// Apply version forcing
		dependencyVersionMap.forEach(
				(ga, v) -> {
					final String[] parts = ga.split( ":" );
					assert parts.length == 2;

					applyConstraint(
							parts[ 0 ],
							parts[ 1 ],
							v,
							configuration,
							project
					);
				}
		);
	}

	private void applyConstraint(String group, String artifact, String version, Configuration configuration, Project project) {
		final String ga = String.format(
				Locale.ROOT,
				"%s:%s",
				group,
				artifact
		);
		final String gav = String.format(
				Locale.ROOT,
				"%s:%s",
				ga,
				version
		);

		configuration.getResolutionStrategy().force( gav );

		final DependencyConstraint dependencyConstraint = project.getDependencies().getConstraints().create(
				ga,
				mutableConstraint -> {
					mutableConstraint.version(
							mutableVersionConstraint -> mutableVersionConstraint.strictly( version )
					);

					// This part below is related to a problem trying to resolve modules which publish Gradle model metadata
					// with matching variants.  The only solution I have found so far is to use some seemingly arbitrary
					// matches based on "variant attributes".
					//
					// todo : need to find a better solution to this

					mutableConstraint.attributes(
							attributeContainer -> {
								attributeContainer.attribute( Usage.USAGE_ATTRIBUTE, JAVA_API_USAGE );
								attributeContainer.attribute( Bundling.BUNDLING_ATTRIBUTE, EXTERNAL_DEP_BUNDLING );
							}
					);
				}
		);
		configuration.getDependencyConstraints().add( dependencyConstraint );
	}

	private Map<String, String> loadDependencyVersionMap() {
		final HashMap<String, String> map = new HashMap<>();

		map.put( "com.github.ben-manes.caffeine:caffeine", "2.8.5" );

		return map;
	}

	public String getQuarkusVersion() {
		// todo : discuss ways to handle this
		//		- settable?
		//		- injectable?
		//		- inherent?
		return "1.7.1.Final";
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

	public NamedDomainObjectContainer<ExtensionDsl> getModules() {
		return modules;
	}

	public void projectEvaluated() {
	}

}
