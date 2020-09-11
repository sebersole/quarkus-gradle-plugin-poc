package org.hibernate.build.gradle.quarkus.extension;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
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
public class ExtensionDslCreator implements NamedDomainObjectFactory<ExtensionDslImplementor>, Serializable {
	private final QuarkusDsl quarkusDsl;

	private final ExtensionModuleCreationListener extensionListener;
	private final Configuration bomConfiguration;

	public ExtensionDslCreator(
			QuarkusDsl quarkusDsl,
			ExtensionModuleCreationListener extensionListener,
			Configuration bomConfiguration) {
		this.quarkusDsl = quarkusDsl;
		this.extensionListener = extensionListener;
		this.bomConfiguration = bomConfiguration;
	}

	@Override
	public ExtensionDslImplementor create(String name) {
		final Project project = quarkusDsl.getProject();
		final DependencyHandler dependencyHandler = project.getDependencies();

		final ExtensionIdentifier extensionIdentifier = ExtensionIdentifier.fromContainerName( name, quarkusDsl );

		// Create a dependency Configuration specific to the extension
		final Configuration extensionDependencyConfiguration = makeExtensionDependencyConfiguration(
				extensionIdentifier,
				quarkusDsl
		);

		// apply the Quarkus BOMs
		extensionDependencyConfiguration.extendsFrom( bomConfiguration );

		// Make the main runtime dependency Configuration extend from this extension Configuration
		// 		- effectively the runtime dependency Configuration is a "live view" over all of the
		// 			individual extension Configurations
		quarkusDsl.getRuntimeConfiguration().extendsFrom( extensionDependencyConfiguration );


		// add the extension being configured as a dependency
		final String extensionGav = Helper.quarkusExtensionCoordinates( extensionIdentifier, quarkusDsl );
		dependencyHandler.add( extensionDependencyConfiguration.getName(), extensionGav );

		// register to resolve the Configuration
		project.afterEvaluate(
				p -> {
					final ResolvedConfiguration resolvedConfiguration = extensionDependencyConfiguration.getResolvedConfiguration();
					resolvedConfiguration.getResolvedArtifacts().forEach(
							resolvedArtifact -> {
								if ( isExtension( resolvedArtifact ) ) {
									//		- create an extension DSL reference

									final ExtensionIdentifier dependencyExtensionIdentifier = ExtensionIdentifier.fromArtifactId(
											resolvedArtifact.getName(),
											quarkusDsl
									);

									if ( dependencyExtensionIdentifier != null ) {
										quarkusDsl.getModules().maybeCreate( dependencyExtensionIdentifier.getDslContainerName() );
									}
								}
							}
					);
				}
		);

		final ExtensionDslImplementor extensionDsl = createExtensionDsl( extensionIdentifier, extensionDependencyConfiguration );
		extensionListener.extensionModuleCreated( extensionDsl );
		return extensionDsl;
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

	private ExtensionDslImplementor createExtensionDsl(
			ExtensionIdentifier extensionIdentifier,
			Configuration extensionDependencyConfiguration) {
		if ( "quarkus-hibernate-orm".equals( extensionIdentifier.getQuarkusArtifactId() ) ) {
			return new HibernateOrmExtensionDsl( extensionIdentifier, extensionDependencyConfiguration, quarkusDsl );
		}

		return new StandardExtensionDsl( extensionIdentifier, extensionDependencyConfiguration, quarkusDsl );
	}

	private static Configuration makeExtensionDependencyConfiguration(
			ExtensionIdentifier extensionIdentifier,
			QuarkusDsl quarkusDsl) {
		final String configName = extensionIdentifier.getCamelCaseName() + "Dependencies";

		final Configuration configuration = quarkusDsl.getProject().getConfigurations().create( configName );
		configuration.setDescription( "Dependencies for the `" + extensionIdentifier.getQuarkusArtifactId() + "` Quarkus extension" );

		return configuration;
	}
}
