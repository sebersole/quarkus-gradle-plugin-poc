package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.dependency.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.extension.ExtensionCreationSupport.createRuntimeDependencyConfiguration;

/**
 * An implicit extension recognized from another extension's dependencies
 */
public class ImplicitExtension extends AbstractExtension {
	public static ImplicitExtension from(
			ModuleVersionIdentifier artifactIdentifier,
			ResolvedDependency transitiveDependency,
			Services services) {
		final Configuration runtimeDependencies = createRuntimeDependencyConfiguration( artifactIdentifier.getArtifactName(), services.getBuildDetails() );
		final Configuration deploymentDependencies = createRuntimeDependencyConfiguration( artifactIdentifier.getArtifactName(), services.getBuildDetails() );

		// `transitiveDependency` is the implicit extension's runtime artifact
		// first, determine its deployment artifact - if one...
		final ResolvedDependency deploymentArtifact = ExtensionCreationSupport.resolveDeploymentArtifact( transitiveDependency, deploymentDependencies, services );

		return new ImplicitExtension( transitiveDependency, deploymentArtifact, runtimeDependencies, deploymentDependencies, services );
	}

	public ImplicitExtension(
			ResolvedDependency runtimeArtifact,
			ResolvedDependency deploymentArtifact,
			Configuration runtimeDependencies,
			Configuration deploymentDependencies,
			Services services) {
		super( runtimeArtifact.getArtifactName(), runtimeArtifact, deploymentArtifact, runtimeDependencies, deploymentDependencies, services );
	}
}
