package org.hibernate.build.gradle.quarkus.extension;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import org.hibernate.build.gradle.quarkus.Helper;
import org.hibernate.build.gradle.quarkus.QuarkusDsl;

import static org.hibernate.build.gradle.quarkus.Helper.EXTENSION_MARKER_FILE;

/**
 * NamedDomainObjectFactory for ExtensionDsl(Implementor) references
 *
 * @author Steve Ebersole
 */
public class ExtensionCreator implements NamedDomainObjectFactory<Extension>, Serializable {
	private final QuarkusDsl quarkusDsl;

	private final ExtensionModuleCreationListener extensionListener;
	private final Configuration bomConfiguration;

	public ExtensionCreator(
			QuarkusDsl quarkusDsl,
			ExtensionModuleCreationListener extensionListener,
			Configuration bomConfiguration) {
		this.quarkusDsl = quarkusDsl;
		this.extensionListener = extensionListener;
		this.bomConfiguration = bomConfiguration;
	}

	@Override
	@SuppressWarnings( "NullableProblems" )
	public Extension create(String name) {
		final Project project = quarkusDsl.getProject();

		final Extension extension = createExtension( name, quarkusDsl );

		extension.getDependencies().extendsFrom( bomConfiguration );
		quarkusDsl.getRuntimeConfiguration().extendsFrom( extension.getDependencies() );

		extensionListener.extensionModuleCreated( extension );

		// register to resolve the Configuration
		project.afterEvaluate(
				p -> {
					final Artifact extensionArtifact = extension.getArtifact();
					if ( extensionArtifact == null ) {
						throw new GradleException( "`Extension#getArtifact` returned null" );
					}

					if ( extensionArtifact.getDependencyNotation() == null ) {
						throw new GradleException( "Extension Artifact did not define dependency" );
					}

					final DependencyHandler dependencyHandler = quarkusDsl.getProject().getDependencies();
					final Dependency extensionArtifactDependency = dependencyHandler.add(
							extension.getDependencies().getName(),
							extensionArtifact.getDependencyNotation(),
							extensionArtifact.getArtifactClosure()
					);

					quarkusDsl.registerExtensionByGav(
							Helper.groupArtifactVersion( extensionArtifactDependency ),
							extension
					);

					final ResolvedConfiguration resolvedConfiguration = extension.getDependencies().getResolvedConfiguration();
					resolvedConfiguration.getResolvedArtifacts().forEach(
							resolvedArtifact -> {
								if ( isExtension( resolvedArtifact ) ) {
									// Create an extension if there is not already one

									final String gav = Helper.groupArtifactVersion( resolvedArtifact );
									final Extension extensionByGav = quarkusDsl.findExtensionByGav( gav );

									if ( extensionByGav != null ) {
										// there is already one - nothing to do...
									}
									else {
										// create it
										final Extension transitiveExtension = new TransitiveExtension(
												resolvedArtifact.getModuleVersion().getId().getName(),
												gav,
												quarkusDsl
										);
										quarkusDsl.getModules().add( transitiveExtension );
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

		return extension;
	}

	private boolean isExtension(ResolvedArtifact resolvedArtifact) {
		if ( ! "pom".equals( resolvedArtifact.getClassifier() ) ) {
			final File artifactFile = resolvedArtifact.getFile();

			try {
				final ZipFile zipFile = new ZipFile( artifactFile );
				final ZipEntry entry = zipFile.getEntry( EXTENSION_MARKER_FILE );
				return entry != null;
			}
			catch ( IOException e ) {
				// not a jar
			}
		}

		return false;
	}

	private static Extension createExtension(
			String containerName,
			QuarkusDsl quarkusDsl) {
		if ( HibernateOrmExtension.CONTAINER_NAME.equals( containerName )
				|| HibernateOrmExtension.ARTIFACT_ID.equals( containerName ) ) {
			return new HibernateOrmExtension( quarkusDsl );
		}

		return new StandardExtension( containerName, quarkusDsl );
	}
}
