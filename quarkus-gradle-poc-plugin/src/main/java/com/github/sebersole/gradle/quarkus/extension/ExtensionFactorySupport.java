package com.github.sebersole.gradle.quarkus.extension;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipFile;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependencyFactory;

import static com.github.sebersole.gradle.quarkus.Helper.EXTENSION_PROP_FILE;

/**
 * Support for factories of Extension references.  Really this boils down
 * to {@link #prepareExtension}
 */
public class ExtensionFactorySupport {
	/**
	 * Applies standard preparation to the extensions
	 */
	public static void prepareExtension(
			Extension extension,
			QuarkusDslImpl quarkusDsl) {
		final Project project = quarkusDsl.getProject();

		// the extension's dependency Configurations extend from the platforms (BOMs)
		extension.getRuntimeDependencies().extendsFrom( quarkusDsl.getPlatforms() );
		extension.getDeploymentDependencies().extendsFrom( quarkusDsl.getPlatforms() );

		// apply the extension's dependency Configurations to the plugin-level Configurations
		quarkusDsl.getRuntimeDependencies().extendsFrom( extension.getRuntimeDependencies() );
		quarkusDsl.getDeploymentDependencies().extendsFrom( extension.getDeploymentDependencies() );


		// register to resolve the Configurations
		project.afterEvaluate(
				p -> resolveDependencies( extension, quarkusDsl )
		);
	}

	public static void resolveDependencies(Extension extension, QuarkusDslImpl quarkusDsl) {
		final DependencyHandler dependencyHandler = quarkusDsl.getProject().getDependencies();

		// handle the "runtime" artifact and "deployment" artifact for the extension
		final String extensionArtifactGav = applyExtensionArtifact( extension, quarkusDsl, dependencyHandler );
		final ResolvedConfiguration resolvedRuntimeDependencies = extension.getRuntimeDependencies().getResolvedConfiguration();


		applyExtensionDeploymentArtifact( extension, quarkusDsl, dependencyHandler );
		extension.getDeploymentDependencies().getResolvedConfiguration();


		// for all runtime dependencies,
		resolvedRuntimeDependencies.getResolvedArtifacts().forEach(
				resolvedArtifact -> {
					final ModuleVersionIdentifier moduleVersionIdentifier = resolvedArtifact.getModuleVersion().getId();
					final String gav = Helper.groupArtifactVersion( moduleVersionIdentifier );

					if ( Objects.equals( gav, extensionArtifactGav ) ) {
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
						quarkusDsl.getBuildState().locateExtensionByGav(
								gav,
								() -> {
									final Extension transitiveExtension = new TransitiveExtension(
											moduleVersionIdentifier.getName(),
											gav,
											quarkusDsl
									);
									quarkusDsl.getQuarkusExtensions().add( transitiveExtension );

									dependencyHandler.add(
											transitiveExtension.getRuntimeDependencies().getName(),
											gav
									);

									// force the dependency resolution
									transitiveExtension.getRuntimeDependencies().getResolvedConfiguration();

									return transitiveExtension;
								}
						);

					}

					// for each resolution, add an entry in the build-state for mapping
					// 		its GAV to a `ResolvedDependency` reference
					quarkusDsl.getBuildState().locateResolvedDependency(
							gav,
							() -> ResolvedDependencyFactory.from( resolvedArtifact, quarkusDsl )
					);
				}
		);
	}

	public static String applyExtensionArtifact(
			Extension extension,
			QuarkusDslImpl quarkusDsl,
			DependencyHandler dependencyHandler) {
		final Artifact extensionArtifact = extension.getArtifact();
		assert extensionArtifact != null : "`Extension(" + extension.getName() + ")#getArtifact` returned null";

		if ( extensionArtifact.getDependency() == null ) {
			throw new GradleException( "Extension(" + extension.getName() + ") Artifact did not define dependency" );
		}

		final Configuration extensionArtifactResolutionScope = quarkusDsl.getProject()
				.getConfigurations()
				.detachedConfiguration( dependencyHandler.create( extensionArtifact.getDependency() ) );

		dependencyHandler.add(
				extension.getRuntimeDependencies().getName(),
				extensionArtifact.getDependency()
		);

		final Set<ResolvedDependency> firstLevelModuleDependencies = extensionArtifactResolutionScope.getResolvedConfiguration().getFirstLevelModuleDependencies();
		final ResolvedDependency resolvedArtifactDependency = extractOnly( extension, firstLevelModuleDependencies, true );

		final String gav = Helper.groupArtifactVersion( resolvedArtifactDependency.getModule().getId() );

		quarkusDsl.getBuildState().registerExtensionByGav( gav, extension );

		return gav;
	}

	public static ResolvedDependency extractOnly(
			Extension extension,
			Set<ResolvedDependency> firstLevelModuleDependencies,
			boolean required) {
		final Iterator<ResolvedDependency> iterator = firstLevelModuleDependencies.iterator();

		if ( ! iterator.hasNext() ) {
			if ( required ) {
				throw new GradleException( "ResolvedDependency set was empty but expecting one value : " + extension.getName() );
			}
			return null;
		}

		final ResolvedDependency resolvedArtifactDependency = iterator.next();

		if ( iterator.hasNext() ) {
			throw new GradleException( "ResolvedDependency set contained multiple values, expecting one : " + extension.getName() );
		}

		return resolvedArtifactDependency;
	}

	public static String applyExtensionDeploymentArtifact(
			Extension extension,
			QuarkusDslImpl quarkusDsl,
			DependencyHandler dependencyHandler) {
		final Artifact extensionDeploymentArtifact = extension.getDeploymentArtifact();

		if ( extensionDeploymentArtifact == null || extensionDeploymentArtifact.getDependency() == null ) {
			return null;
		}

		final Dependency deploymentArtifactDependency = dependencyHandler.create( extensionDeploymentArtifact.getDependency() );


		// we do not necessarily know whether this deployment artifact exists, so make it's resolution lenient

		final Configuration deploymentArtifactResolutionScope = quarkusDsl.getProject()
				.getConfigurations()
				.detachedConfiguration( deploymentArtifactDependency );
		deploymentArtifactResolutionScope.extendsFrom( quarkusDsl.getPlatforms() );

		final LenientConfiguration lenientConfiguration = deploymentArtifactResolutionScope.getResolvedConfiguration().getLenientConfiguration();
		final ResolvedDependency resolvedDependency = extractOnly( extension, lenientConfiguration.getFirstLevelModuleDependencies(), false );
		if ( resolvedDependency == null ) {
			return null;
		}

		// it exists... add it to the extension's deployment dependencies
		dependencyHandler.add(
				extension.getDeploymentDependencies().getName(),
				extensionDeploymentArtifact.getDependency()
		);

		return Helper.groupArtifactVersion( resolvedDependency.getModule().getId() );
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
}
