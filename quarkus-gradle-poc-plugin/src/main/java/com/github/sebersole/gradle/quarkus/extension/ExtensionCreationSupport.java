package com.github.sebersole.gradle.quarkus.extension;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipFile;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;
import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.dependency.DependencyHelper;
import com.github.sebersole.gradle.quarkus.dependency.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.dependency.StandardModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.DEPLOYMENT_ARTIFACT_KEY;
import static com.github.sebersole.gradle.quarkus.Helper.EXTENSION_PROP_FILE;

/**
 * Support for creation of Extension references.
 */
public class ExtensionCreationSupport {

	public static void resolveDependencies(Extension extension, Services services) {
		resolveRuntimeDependencies( extension, services );
	}

	private static void resolveRuntimeDependencies(Extension extension, Services services) {
		Logging.LOGGER.debug( "Resolving runtime dependencies for `{}` extension", extension.getDslName() );

		final ExtensionService extensionService = services.getExtensionService();

		final com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency extensionRuntimeArtifact = extension.getArtifact();

		// visit each runtime dependency for the artifact...
		extension.getRuntimeDependencies().getResolvedConfiguration().getResolvedArtifacts().forEach(
				resolvedArtifact -> {
					final ModuleVersionIdentifier artifactIdentifier = new StandardModuleVersionIdentifier( resolvedArtifact );
					final com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency resolvedRuntimeDependency = DependencyHelper.registerDependency( artifactIdentifier, resolvedArtifact, services );

					// todo : should this comparison be based on `group:artifact` rather than `group:artifact:version`?
					//		really we should never have conflicting "module versions", right?
					if ( Objects.equals( artifactIdentifier, extensionRuntimeArtifact.getModuleVersionIdentifier() ) ) {
						// `resolvedArtifact` is the extension's runtime artifact
						final Object marker = extractExtensionMarker( resolvedArtifact );
						if ( marker == null ) {
							Logging.LOGGER.warn(
									String.format(
											Locale.ROOT,
											"Extension artifact did not define extension marker (%s) : %s (%s)",
											EXTENSION_PROP_FILE,
											extension.getDslName(),
											resolvedArtifact.getId().getComponentIdentifier().getDisplayName()
									)
							);
						}
					}
					else if ( isExtension( resolvedArtifact ) ) {
						// `resolvedArtifact` is an implied extension - create an extension if
						// 		there is not already one

						final Extension existing = extensionService.findByModule( artifactIdentifier );
						if ( existing == null ) {
							// create one...
							final Extension transitiveExtension = ImplicitExtension.from( artifactIdentifier, resolvedRuntimeDependency, services );

							extensionService.registerExtension( transitiveExtension );

							services.getBuildDetails().getMainProject().getDependencies().add(
									transitiveExtension.getRuntimeDependencies().getName(),
									artifactIdentifier.groupArtifactVersion()
							);

							ExtensionCreationSupport.resolveDependencies( transitiveExtension, services );
						}
					}
				}
		);
	}


	private static boolean isExtension(ResolvedArtifact resolvedArtifact) {
		if ( ! "pom".equals( resolvedArtifact.getClassifier() ) ) {
			final Object marker = extractExtensionMarker( resolvedArtifact );
			return marker != null;
		}

		return false;
	}

	private static Object extractExtensionMarker(ResolvedArtifact resolvedArtifact) {
		final File artifactFile = resolvedArtifact.getFile();

		if ( artifactFile.isDirectory() ) {
			// try to find it relative to the directory
			final File marker = new File( artifactFile, EXTENSION_PROP_FILE );
			if ( marker.exists() ) {
				return marker;
			}

			return null;
		}

		// try as a JAR
		try {
			final ZipFile zipFile = new ZipFile( artifactFile );
			return zipFile.getEntry( EXTENSION_PROP_FILE );
		}
		catch ( IOException e ) {
			// not a jar
		}

		return null;
	}

	public static Configuration createRuntimeDependencyConfiguration(String name, BuildDetails buildDetails) {
		final Configuration runtimeDependencies = buildDetails.getMainProject().getConfigurations().maybeCreate( name + "Runtime" );
		runtimeDependencies.setDescription( "Runtime dependencies for the `" + name + "` Quarkus extension" );
		runtimeDependencies.extendsFrom( buildDetails.getPlatforms() );
		buildDetails.getRuntimeDependencies().extendsFrom( runtimeDependencies );

		return runtimeDependencies;
	}

	public static Configuration createDeploymentDependencyConfiguration(String name, BuildDetails buildDetails) {
		final Configuration deploymentDependencies = buildDetails.getMainProject().getConfigurations().maybeCreate( name + "Deployment" );
		deploymentDependencies.setDescription( "Deployment dependencies for the `" + name + "` Quarkus extension" );
		deploymentDependencies.extendsFrom( buildDetails.getPlatforms() );
		buildDetails.getDeploymentDependencies().extendsFrom( deploymentDependencies );

		return deploymentDependencies;
	}

	public static com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency resolveRuntimeArtifact(
			Dependency runtimeArtifact,
			Configuration runtimeDependencies,
			Services services) {
		final BuildDetails buildDetails = services.getBuildDetails();
		final Project mainProject = buildDetails.getMainProject();

		mainProject.getDependencies().add( runtimeDependencies.getName(), runtimeArtifact );

		if ( runtimeArtifact instanceof ProjectDependency ) {
			return services.getDependencyService().registerProjectDependency( (ProjectDependency) runtimeArtifact );
		}
		else {
			final ConfigurationContainer configurations = mainProject.getConfigurations();
			final Configuration extensionArtifactResolutionScope = configurations.detachedConfiguration( runtimeArtifact );

			final Set<org.gradle.api.artifacts.ResolvedDependency> firstLevelModuleDependencies = extensionArtifactResolutionScope.getResolvedConfiguration().getFirstLevelModuleDependencies();
			final org.gradle.api.artifacts.ResolvedDependency resolvedArtifactDependency = Helper.extractOnlyOne(
					firstLevelModuleDependencies,
					() -> {
						throw new GradleException( "ResolvedDependency set was empty but expecting one value : " + Helper.groupArtifactVersion( runtimeArtifact ) );
					},
					() -> {
						throw new GradleException( "Expecting single ResolvedDependency but found multiple : " + Helper.groupArtifactVersion( runtimeArtifact ) );
					}
			);
			assert resolvedArtifactDependency != null;
			return services.getDependencyService().registerExternalDependency( resolvedArtifactDependency );
		}
	}

	public static com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency resolveDeploymentArtifact(
			com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency runtimeArtifact,
			Configuration deploymentDependencies,
			Services services) {

		final Properties properties = runtimeArtifact.extensionMarkerPropertiesAccess().get();
		final String deploymentArtifactDependencyNotation = properties.getProperty( DEPLOYMENT_ARTIFACT_KEY );

		if ( deploymentArtifactDependencyNotation == null ) {
			return null;
		}

		final BuildDetails buildDetails = services.getBuildDetails();
		final Project mainProject = buildDetails.getMainProject();

		final Dependency deploymentArtifact = mainProject.getDependencies().create( deploymentArtifactDependencyNotation );
		mainProject.getDependencies().add( deploymentDependencies.getName(), deploymentArtifact );

		final ConfigurationContainer configurations = mainProject.getConfigurations();
		final Configuration extensionArtifactResolutionScope = configurations.detachedConfiguration( deploymentArtifact );

		final Set<org.gradle.api.artifacts.ResolvedDependency> firstLevelModuleDependencies = extensionArtifactResolutionScope.getResolvedConfiguration().getFirstLevelModuleDependencies();
		final org.gradle.api.artifacts.ResolvedDependency resolvedArtifactDependency = Helper.extractOnlyOne(
				firstLevelModuleDependencies,
				() -> {
					throw new GradleException( "ResolvedDependency set was empty but expecting one value : " + Helper.groupArtifactVersion( deploymentArtifact ) );
				},
				() -> {
					throw new GradleException( "Expecting single ResolvedDependency but found multiple : " + Helper.groupArtifactVersion( deploymentArtifact ) );
				}
		);
		assert resolvedArtifactDependency != null;
		return services.getDependencyService().registerExternalDependency( resolvedArtifactDependency );
	}
}
