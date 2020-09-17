package com.github.sebersole.gradle.quarkus.extension;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.zip.ZipFile;

import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependencyFactory;

import static com.github.sebersole.gradle.quarkus.Helper.EXTENSION_MARKER_FILE;

/**
 * NamedDomainObjectFactory for ExtensionDsl(Implementor) references
 *
 * @author Steve Ebersole
 */
public class ExtensionFactory implements NamedDomainObjectFactory<Extension>, Serializable {
	private final QuarkusDslImpl quarkusDsl;

	private final Configuration bomConfiguration;

	public ExtensionFactory(QuarkusDslImpl quarkusDsl, Configuration bomConfiguration) {
		this.quarkusDsl = quarkusDsl;
		this.bomConfiguration = bomConfiguration;
	}

	@Override
	@SuppressWarnings( "NullableProblems" )
	public Extension create(String name) {
		final Extension extension = createExtension( name, quarkusDsl );

		prepareExtension( extension, quarkusDsl );

		return extension;
	}

	public <S extends Extension> S create(String name, Class<S> extensionImpl) {
		final S extension = createExtension( name, extensionImpl, quarkusDsl );

		prepareExtension( extension, quarkusDsl );

		return extension;
	}

	public void prepareExtension(Extension extension, QuarkusDsl quarkusDsl) {
		prepareExtension( extension, (QuarkusDslImpl) quarkusDsl );
	}

	public void prepareExtension(Extension extension, QuarkusDslImpl quarkusDsl) {
		final Project project = quarkusDsl.getProject();

		extension.getRuntimeDependencies().extendsFrom( bomConfiguration );
		extension.getDeploymentDependencies().extendsFrom( bomConfiguration );

		quarkusDsl.getRuntimeDependencies().extendsFrom( extension.getRuntimeDependencies() );
		quarkusDsl.getDeploymentDependencies().extendsFrom( extension.getDeploymentDependencies() );

		// register to resolve the Configuration
		project.afterEvaluate(
				p -> {
					final Artifact extensionArtifact = extension.getArtifact();
					assert extensionArtifact != null : "`Extension#getArtifact` returned null";

					if ( extensionArtifact.getDependency() == null ) {
						throw new GradleException( "Extension Artifact did not define dependency" );
					}

					final DependencyHandler dependencyHandler = quarkusDsl.getProject().getDependencies();
					final Dependency extensionArtifactDependency = dependencyHandler.add(
							extension.getRuntimeDependencies().getName(),
							extensionArtifact.getDependency()
					);

					assert extensionArtifactDependency != null;

					quarkusDsl.getBuildState().registerExtensionByGav(
							Helper.groupArtifactVersion( extensionArtifactDependency ),
							extension
					);

					final ResolvedConfiguration resolvedRuntimeDependencies = extension.getRuntimeDependencies().getResolvedConfiguration();
					resolvedRuntimeDependencies.getResolvedArtifacts().forEach(
							resolvedArtifact -> {
								final ModuleVersionIdentifier moduleVersionIdentifier = resolvedArtifact.getModuleVersion().getId();
								final String gav = Helper.groupArtifactVersion( moduleVersionIdentifier );

								if ( areSame( moduleVersionIdentifier, extensionArtifactDependency ) ) {
									final Object marker = extractExtensionMarker( resolvedArtifact );
									if ( marker == null ) {
										Logging.LOGGER.warn(
												String.format(
														Locale.ROOT,
														"Extension artifact did not define extension marker (%s) : %s (%s)",
														EXTENSION_MARKER_FILE,
														extension.getDslName(),
														resolvedArtifact.getId().getComponentIdentifier().getDisplayName()
												)
										);
									}
								}
								else if ( isExtension( resolvedArtifact ) ) {
									// Create an extension if there is not already one

									final Extension extensionByGav = quarkusDsl.getBuildState().findExtensionByGav( gav );

									//noinspection StatementWithEmptyBody
									if ( extensionByGav != null ) {
										// there is already one - nothing to do...
									}
									else {
										// create it
										final Extension transitiveExtension = new TransitiveExtension(
												moduleVersionIdentifier.getName(),
												gav,
												quarkusDsl
										);
										quarkusDsl.getQuarkusExtensions().add( transitiveExtension );
										quarkusDsl.getBuildState().registerExtensionByGav( gav, transitiveExtension );

										dependencyHandler.add(
												transitiveExtension.getRuntimeDependencies().getName(),
												gav
										);

										// force the dependency resolution
										transitiveExtension.getRuntimeDependencies().getResolvedConfiguration();
									}
								}

								quarkusDsl.getBuildState().locateResolvedDependency(
										gav,
										() -> ResolvedDependencyFactory.from( resolvedArtifact, quarkusDsl )
								);
							}
					);

					final Artifact extensionDeploymentArtifact = extension.getDeploymentArtifact();
					if ( extensionDeploymentArtifact != null && extensionDeploymentArtifact.getDependency() != null ) {
						// we do not necessarily know whether this deployment artifact exists, so make it's resolution lenient
						dependencyHandler.add(
								extension.getDeploymentDependencies().getName(),
								extensionDeploymentArtifact.getDependency()
						);
					}
					// force the (lenient) resolution of the deployment dependencies
					extension.getDeploymentDependencies().getResolvedConfiguration().getLenientConfiguration();
				}
		);
	}

	private boolean areSame(ModuleVersionIdentifier resolvedInfo, Dependency extensionArtifactDependency) {
		if ( ! resolvedInfo.getGroup().equals( extensionArtifactDependency.getGroup() ) ) {
			return false;
		}

		//noinspection RedundantIfStatement
		if ( ! resolvedInfo.getName().equals( extensionArtifactDependency.getName() ) ) {
			return false;
		}

		// does version matter?  should not - really should have been "forced" (BOM)

		return true;
	}

	private boolean isExtension(ResolvedArtifact resolvedArtifact) {
		if ( ! "pom".equals( resolvedArtifact.getClassifier() ) ) {
			final Object marker = extractExtensionMarker( resolvedArtifact );
			return marker != null;
		}

		return false;
	}

	private Object extractExtensionMarker(ResolvedArtifact resolvedArtifact) {
		final File artifactFile = resolvedArtifact.getFile();

		if ( artifactFile.isDirectory() ) {
			// try to find it relative to the directory
			final File marker = new File( artifactFile, EXTENSION_MARKER_FILE );
			if ( marker.exists() ) {
				return marker;
			}

			return null;
		}

		// try as a JAR
		try {
			final ZipFile zipFile = new ZipFile( artifactFile );
			return zipFile.getEntry( EXTENSION_MARKER_FILE );
		}
		catch ( IOException e ) {
			// not a jar
		}

		return null;
	}

	private static Extension createExtension(
			String containerName,
			QuarkusDslImpl quarkusDsl) {
		if ( HibernateOrmExtension.CONTAINER_NAME.equals( containerName )
				|| HibernateOrmExtension.ARTIFACT_SHORT_NAME.equals( containerName ) ) {
			return new HibernateOrmExtension( quarkusDsl );
		}

		return new StandardExtension( containerName, quarkusDsl );
	}

	private static <S extends Extension> S createExtension(
			String containerName,
			Class<S> extensionImpl,
			QuarkusDsl quarkusDsl) {
		try {
			return extensionImpl.getDeclaredConstructor().newInstance();
		}
		catch ( Exception e ) {
			throw new InvalidUserDataException( "Could not create explicitly typed Extension", e );
		}
	}

	public HibernateOrmExtension hibernateOrm(QuarkusDslImpl quarkusDsl) {
		final HibernateOrmExtension extension = new HibernateOrmExtension( quarkusDsl );
		prepareExtension( extension, quarkusDsl );
		return extension;
	}
}
