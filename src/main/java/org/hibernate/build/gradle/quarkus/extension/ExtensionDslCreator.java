package org.hibernate.build.gradle.quarkus.extension;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import org.hibernate.build.gradle.quarkus.Helper;
import org.hibernate.build.gradle.quarkus.QuarkusDsl;

import static java.util.Arrays.asList;
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

	private final HashSet<String> nonExtensionQuarkusArtifactIds = new HashSet<>(
			asList( "quarkus-core", Helper.QUARKUS_BOM )
	);

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
				quarkusDsl,
				nonExtensionQuarkusArtifactIds
		);

		// apply the Quarkus BOM
		extensionDependencyConfiguration.extendsFrom( bomConfiguration );

		// Make the main QuarkusDsl dependency Configuration extend (think inherit) from this extension Configuration
		// 		- effectively the QuarkusDsl dependency Configuration is a "live view" over all of the individual extension Configurations
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
								if ( ! "pom".equals( resolvedArtifact.getClassifier() ) ) {
									final File artifactFile = resolvedArtifact.getFile();
									if ( isExtension( artifactFile ) ) {
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
							}
					);
				}
		);

		final ExtensionDslImplementor extensionDsl = createExtensionDsl( extensionIdentifier, extensionDependencyConfiguration );
		extensionListener.extensionModuleCreated( extensionDsl );
		return extensionDsl;
	}

	private boolean isExtension(File artifactFile) {
		try {
			final ZipFile zipFile = new ZipFile( artifactFile );
			final ZipEntry entry = zipFile.getEntry( EXTENSION_MARKER_FILE );
			return entry != null;
		}
		catch ( IOException e ) {
			// not a jar?
			return false;
		}
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
			QuarkusDsl quarkusDsl,
			HashSet<String> nonExtensionQuarkusArtifactIds) {
		final String configName = extensionIdentifier.getCamelCaseName() + "Dependencies";

		final Configuration configuration = quarkusDsl.getProject().getConfigurations().create( configName );
		configuration.setDescription( "Dependencies for the `" + extensionIdentifier.getQuarkusArtifactId() + "` Quarkus extension" );

//		// apply handling for Quarkus extensions discovered as part of the dependency graph
//		configuration.resolutionStrategy(
//				resolutionStrategy -> {
//					resolutionStrategy.eachDependency(
//							dependencyResolveDetails -> {
//								final ModuleVersionSelector requestedDetails = dependencyResolveDetails.getRequested();
//
//								if ( "io.quarkus".equals( requestedDetails.getGroup() ) ) {
//									// we have a Quarkus artifact...
//									if ( ! nonExtensionQuarkusArtifactIds.contains( requestedDetails.getName() ) ) {
//										// and it is an "extension" - todo : other criteria to consider here?
//										//		- create an extension DSL reference
//
//										final ExtensionIdentifier dependencyExtensionIdentifier = ExtensionIdentifier.fromArtifactId(
//												requestedDetails.getName(),
//												quarkusDsl
//										);
//
//										if ( dependencyExtensionIdentifier != null ) {
//											quarkusDsl.getModules().maybeCreate( dependencyExtensionIdentifier.getDslContainerName() );
//										}
//									}
//								}
//							}
//					);
//				}
//		);

		return configuration;

//
//
//
//
//		final Usage javaApiUsage = quarkusDsl.getProject().getObjects().named( Usage.class, Usage.JAVA_API );
//		final Bundling externalBundling = quarkusDsl.getProject().getObjects().named( Bundling.class, Bundling.EXTERNAL );
//
//		return quarkusDsl.getProject().getConfigurations().create(
//				configName,
//				(config) -> {
//					config.setDescription( "Dependencies for the `" + extensionIdentifier.getQuarkusArtifactId() + "` Quarkus extension" );
//
//					config.resolutionStrategy(
//							resolutionStrategy -> {
////								quarkusDsl.getVersionBomMap().forEach(
////										(dependencyGroupArtifact, forcedVersion) -> {
////											final String[] parts = dependencyGroupArtifact.split( ":" );
////											assert parts.length == 2;
////
////											final String gav = Helper.groupArtifactVersion( parts[ 0 ], parts[ 1 ], forcedVersion );
////											final String ga = Helper.groupArtifact( parts[ 0 ], parts[ 1 ] );
////
////											resolutionStrategy.force( gav );
////
////											final DependencyConstraint dependencyConstraint = quarkusDsl.getProject().getDependencies().getConstraints().create(
////													ga,
////													mutableConstraint -> {
////														mutableConstraint.version(
////																mutableVersionConstraint -> mutableVersionConstraint.strictly( forcedVersion )
////														);
////
////														// This part below is related to a problem trying to resolve modules which publish Gradle model metadata
////														// with matching variants.  The only solution I have found so far is to use some seemingly arbitrary
////														// matches based on "variant attributes".
////														//
////														// todo : need to find a better solution to this
////
////														mutableConstraint.attributes(
////																attributeContainer -> {
////																	attributeContainer.attribute( Usage.USAGE_ATTRIBUTE, javaApiUsage );
////																	attributeContainer.attribute( Bundling.BUNDLING_ATTRIBUTE, externalBundling );
////																}
////														);
////													}
////											);
////											config.getDependencyConstraints().add( dependencyConstraint );
////										}
////								);
//
//								resolutionStrategy.eachDependency(
//										dependencyResolveDetails -> {
//											// check each dependency to see if it is a Quarkus extension, and if so
//											// create an extension config dsl reference for it in the plugin
//											final ModuleVersionSelector requestedDetails = dependencyResolveDetails.getRequested();
//
//											if ( "io.quarkus".equals( requestedDetails.getGroup() ) ) {
//												// for every Quarkus module:
//
//												//		1) force version
//												dependencyResolveDetails.useVersion( quarkusDsl.getQuarkusVersion() );
//
//												//		2) create an extension DSL for it.  NOTE that atm
//												//			we do not really know the difference between a normal Quarkus module
//												//			and an "extension".  For the time being I simply apply to all
//												// 			in the Quarkus group.
//												// 			todo : exclude quarkus-core, etc?
//
//												final ExtensionIdentifier dependencyExtensionIdentifier = ExtensionIdentifier.fromArtifactId(
//														requestedDetails.getName(),
//														quarkusDsl
//												);
//
//												if ( dependencyExtensionIdentifier != null ) {
//													quarkusDsl.getModules().maybeCreate( dependencyExtensionIdentifier.getDslContainerName() );
//												}
//											}
//										}
//								);
//							}
//					);
//				}
//		);
	}
}
