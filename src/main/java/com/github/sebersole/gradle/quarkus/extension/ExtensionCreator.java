package com.github.sebersole.gradle.quarkus.extension;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.zip.ZipFile;

import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectContainer;
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

import static com.github.sebersole.gradle.quarkus.Helper.EXTENSION_MARKER_FILE;

/**
 * NamedDomainObjectFactory for ExtensionDsl(Implementor) references
 *
 * @author Steve Ebersole
 */
public class ExtensionCreator implements NamedDomainObjectFactory<Extension>, Serializable {
	private final QuarkusDsl quarkusDsl;

	private final Configuration bomConfiguration;

	public ExtensionCreator(QuarkusDsl quarkusDsl, Configuration bomConfiguration) {
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
		final Project project = quarkusDsl.getProject();

		extension.getDependencies().extendsFrom( bomConfiguration );
		quarkusDsl.getRuntimeConfiguration().extendsFrom( extension.getDependencies() );

		// register to resolve the Configuration
		project.afterEvaluate(
				p -> {
					final Artifact extensionArtifact = extension.getArtifact();
					if ( extensionArtifact == null ) {
						throw new GradleException( "`Extension#getArtifact` returned null" );
					}

					if ( extensionArtifact.getDependency() == null ) {
						throw new GradleException( "Extension Artifact did not define dependency" );
					}

					final DependencyHandler dependencyHandler = quarkusDsl.getProject().getDependencies();
					final Dependency extensionArtifactDependency = dependencyHandler.add(
							extension.getDependencies().getName(),
							extensionArtifact.getDependency()
					);

					assert extensionArtifactDependency != null;

					quarkusDsl.registerExtensionByGav(
							Helper.groupArtifactVersion( extensionArtifactDependency ),
							extension
					);

					final ResolvedConfiguration resolvedConfiguration = extension.getDependencies().getResolvedConfiguration();
					resolvedConfiguration.getResolvedArtifacts().forEach(
							resolvedArtifact -> {
								final ModuleVersionIdentifier moduleVersionIdentifier = resolvedArtifact.getModuleVersion().getId();

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

									final String gav = Helper.groupArtifactVersion( resolvedArtifact );
									final Extension extensionByGav = quarkusDsl.findExtensionByGav( gav );

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
										( (NamedDomainObjectContainer) quarkusDsl.getModules() ).add( transitiveExtension );
										quarkusDsl.registerExtensionByGav( gav, transitiveExtension );

										dependencyHandler.add(
												transitiveExtension.getDependencies().getName(),
												gav
										);

										// force the dependency resolution
										transitiveExtension.getDependencies().getResolvedConfiguration();
									}
								}
							}
					);
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
			QuarkusDsl quarkusDsl) {
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

	public HibernateOrmExtension hibernateOrm(QuarkusDsl quarkusDsl) {
		final HibernateOrmExtension extension = new HibernateOrmExtension( quarkusDsl );
		prepareExtension( extension, quarkusDsl );
		return extension;
	}
}
