package org.hibernate.build.gradle.quarkus.extension;

import java.io.Serializable;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Usage;

import org.hibernate.build.gradle.quarkus.Helper;
import org.hibernate.build.gradle.quarkus.QuarkusDsl;

/**
 * NamedDomainObjectFactory for ExtensionDsl(Implementor) references
 *
 * @author Steve Ebersole
 */
public class ExtensionDslCreator implements NamedDomainObjectFactory<ExtensionDslImplementor>, Serializable {
	private final QuarkusDsl quarkusDsl;

	public ExtensionDslCreator(QuarkusDsl quarkusDsl) {
		this.quarkusDsl = quarkusDsl;
	}

	@Override
	public ExtensionDslImplementor create(String name) {
		final ExtensionIdentifier extensionIdentifier = ExtensionIdentifier.fromContainerName( name, quarkusDsl );

		// Create a dependency Configuration specific to the extension
		final Configuration extensionDependencyConfiguration = makeExtensionDependencyConfiguration( extensionIdentifier, quarkusDsl );
		quarkusDsl.getProject().afterEvaluate( project -> extensionDependencyConfiguration.resolve() );

		// Make the main QuarkusDsl dependency Configuration "extend" from this extension Configuration
		// 		- effectively the QuarkusDsl dependency Configuration is a "live view" over all of
		//			the individual extension Configurations
		quarkusDsl.getRuntimeConfiguration().extendsFrom( extensionDependencyConfiguration );

		return createExtensionDsl( extensionIdentifier, extensionDependencyConfiguration );
	}

	private ExtensionDslImplementor createExtensionDsl(
			ExtensionIdentifier extensionIdentifier,
			Configuration extensionDependencyConfiguration) {
		if ( "quarkus-hibernate-orm".equals( extensionIdentifier.getQuarkusArtifactId() ) ) {
			return new HibernateOrmExtensionDsl( extensionIdentifier, extensionDependencyConfiguration, quarkusDsl );
		}

		return new StandardExtensionDsl( extensionIdentifier, extensionDependencyConfiguration, quarkusDsl );
	}

	public static Configuration makeExtensionDependencyConfiguration(ExtensionIdentifier extensionIdentifier, QuarkusDsl quarkusDsl) {
		final String configName = extensionIdentifier.getCamelCaseName() + "Dependencies";

		final Usage javaApiUsage = quarkusDsl.getProject().getObjects().named( Usage.class, Usage.JAVA_API );
		final Bundling externalBundling = quarkusDsl.getProject().getObjects().named( Bundling.class, Bundling.EXTERNAL );

		return quarkusDsl.getProject().getConfigurations().create(
				configName,
				(config) -> {
					config.setDescription( "Dependencies for the `" + extensionIdentifier.getQuarkusArtifactId() + "` Quarkus extension" );

					config.resolutionStrategy(
							resolutionStrategy -> {
								quarkusDsl.getVersionBomMap().forEach(
										(dependencyGroupArtifact, forcedVersion) -> {
											final String[] parts = dependencyGroupArtifact.split( ":" );
											assert parts.length == 2;

											final String gav = Helper.groupArtifactVersion( parts[ 0 ], parts[ 1 ], forcedVersion );
											final String ga = Helper.groupArtifact( parts[ 0 ], parts[ 1 ] );

											resolutionStrategy.force( gav );

											final DependencyConstraint dependencyConstraint = quarkusDsl.getProject().getDependencies().getConstraints().create(
													ga,
													mutableConstraint -> {
														mutableConstraint.version(
																mutableVersionConstraint -> mutableVersionConstraint.strictly( forcedVersion )
														);

														// This part below is related to a problem trying to resolve modules which publish Gradle model metadata
														// with matching variants.  The only solution I have found so far is to use some seemingly arbitrary
														// matches based on "variant attributes".
														//
														// todo : need to find a better solution to this

														mutableConstraint.attributes(
																attributeContainer -> {
																	attributeContainer.attribute( Usage.USAGE_ATTRIBUTE, javaApiUsage );
																	attributeContainer.attribute( Bundling.BUNDLING_ATTRIBUTE, externalBundling );
																}
														);
													}
											);
											config.getDependencyConstraints().add( dependencyConstraint );
										}
								);

								resolutionStrategy.eachDependency(
										dependencyResolveDetails -> {
											// check each dependency to see if it is a Quarkus extension, and if so
											// create an extension config dsl reference for it in the plugin
											final ModuleVersionSelector requestedDetails = dependencyResolveDetails.getRequested();

											if ( "io.quarkus".equals( requestedDetails.getGroup() ) ) {
												// for every Quarkus module:

												//		1) force version
												dependencyResolveDetails.useVersion( quarkusDsl.getQuarkusVersion() );

												//		2) create an extension DSL for it.  NOTE that atm
												//			we do not really know the difference between a normal Quarkus module
												//			and an "extension".  For the time being I simply apply to all
												// 			in the Quarkus group.
												// 			todo : exclude quarkus-core, etc?

												final ExtensionIdentifier dependencyExtensionIdentifier = ExtensionIdentifier.fromArtifactId(
														requestedDetails.getName(),
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
				}
		);
	}
}
