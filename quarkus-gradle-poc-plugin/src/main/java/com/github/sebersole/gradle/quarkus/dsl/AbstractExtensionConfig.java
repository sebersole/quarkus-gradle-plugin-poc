package com.github.sebersole.gradle.quarkus.dsl;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.extension.Convertible;
import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.extension.StandardExtension;
import com.github.sebersole.gradle.quarkus.service.Services;
import groovy.lang.Closure;

import static com.github.sebersole.gradle.quarkus.extension.ExtensionCreationSupport.createDeploymentDependencyConfiguration;
import static com.github.sebersole.gradle.quarkus.extension.ExtensionCreationSupport.createRuntimeDependencyConfiguration;
import static com.github.sebersole.gradle.quarkus.extension.ExtensionCreationSupport.resolveDeploymentArtifact;
import static com.github.sebersole.gradle.quarkus.extension.ExtensionCreationSupport.resolveRuntimeArtifact;

/**
 * Base support for ExtensionConfig implementations.
 *
 * @implNote The runtime and deployment artifacts are kept separate from the
 * runtime and deployment dependency Configurations kept here.  The Configurations
 * serve 2 purposes: (1) to allow users to provide extension-specific additional
 * dependencies (not sure that is useful) and (2) used while resolving the
 * extension-config into its Extension to resolve the runtime/deployment artifact
 * plus any of the "extra" dependencies
 */
public abstract class AbstractExtensionConfig implements ExtensionConfig, Convertible<Extension> {
	private final String name;
	private final BuildDetails buildDetails;

	private final Configuration runtimeDependencies;

	private Dependency runtimeArtifact;

	public AbstractExtensionConfig(String name, BuildDetails buildDetails) {
		this.name = name;
		this.buildDetails = buildDetails;
		this.runtimeDependencies = createRuntimeDependencyConfiguration( name, buildDetails );

	}

	public AbstractExtensionConfig(
			String name,
			BuildDetails buildDetails,
			Object runtimeArtifactNotation) {
		this( name, buildDetails );

		artifact( runtimeArtifactNotation );
	}

	@Override
	public String getDslName() {
		return name;
	}

	public Configuration getRuntimeDependencies() {
		return runtimeDependencies;
	}

	@Override
	public Dependency getRuntimeArtifact() {
		return runtimeArtifact;
	}

	@Override
	public Dependency artifact(Object notation) {
		final DependencyHandler dependencyHandler = buildDetails.getMainProject().getDependencies();
		this.runtimeArtifact = dependencyHandler.create( notation );
		return runtimeArtifact;
	}

	@Override
	public void artifact(Object notation, Closure<Dependency> closure) {
		final DependencyHandler dependencyHandler = buildDetails.getMainProject().getDependencies();
		this.runtimeArtifact = dependencyHandler.create( notation, closure );
	}

	@Override
	public void artifact(Object notation, Action<Dependency> action) {
		action.execute( artifact( notation ) );
	}

	@Override
	public Extension convert(Services services) {
		final ResolvedDependency resolvedRuntimeDependency = resolveRuntimeArtifact( runtimeArtifact, runtimeDependencies, services );

		final Configuration deploymentDependencies = createDeploymentDependencyConfiguration( name, buildDetails );
		final ResolvedDependency resolvedDeploymentDependency = resolveDeploymentArtifact( resolvedRuntimeDependency, deploymentDependencies, services );

		return new StandardExtension(
				getDslName(),
				resolvedRuntimeDependency,
				resolvedDeploymentDependency,
				runtimeDependencies,
				deploymentDependencies,
				services
		);
	}
}
