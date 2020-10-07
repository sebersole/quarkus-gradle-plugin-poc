package com.github.sebersole.gradle.quarkus.extension;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.gradle.api.PolymorphicDomainObjectContainer;

import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.dependency.ModuleIdentifier;
import com.github.sebersole.gradle.quarkus.dependency.StrictModuleIdentifierComparator;
import com.github.sebersole.gradle.quarkus.dsl.ExtensionSpec;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Service for handling Quarkus extensions
 */
public class ExtensionService implements Serializable {
	private final Services services;
	private final Map<String,Extension> extensionsByName;
	private final Map<ModuleIdentifier, Extension> extensionsByModule;

	public ExtensionService(Services services) {
		this.services = services;
		this.extensionsByName = new HashMap<>();
		this.extensionsByModule = new TreeMap<>( new StrictModuleIdentifierComparator() );
	}

	public void resolve(PolymorphicDomainObjectContainer<? extends ExtensionSpec> extensionSpecs) {
		// `extensionConfigs` contains all of the explicitly declared extensions...
		// resolve them, including their dependencies.  Resolving the dependencies
		// may trigger implicit extensions (recursive)

		extensionSpecs.forEach(
				extensionSpec -> {
					//noinspection rawtypes
					final Extension extension = ((Convertible) extensionSpec).convert( services );

					// register the extension and then resolve its dependencies
					services.getExtensionService().registerExtension( extension );
					ExtensionCreationSupport.resolveDependencies( extension, services );
				}
		);
	}

	public void registerExtension(Extension extension) {
		Logging.LOGGER.debug( "Registering Extension : {}", extension );

		// add the extension to the `extensions` Set as well as registering it by both its
		// runtime and deployment (if provided) artifact

		extensionsByName.put( extension.getDslName(), extension );

		extensionsByModule.put( extension.getArtifact().getModuleVersionIdentifier(), extension );

		if ( extension.getDeploymentArtifact() != null ) {
			extensionsByModule.put( extension.getDeploymentArtifact().getModuleVersionIdentifier(), extension );
		}
	}

	public Extension findByName(String name) {
		return extensionsByName.get( name );
	}

	public Extension getByName(String name) {
		final Extension byName = findByName( name );
		if ( byName == null ) {
			throw new IllegalArgumentException( "No extension registered under name `" + name + "`" );
		}
		return byName;
	}

	/**
	 * Find an extension by either its runtime or deployment module (group:name) identifier
	 */
	public Extension findByModule(ModuleIdentifier identifier) {
		return extensionsByModule.get( identifier );
	}

	public void visitExtensions(Consumer<Extension> visitor) {
		extensionsByName.forEach( (name, extension) -> visitor.accept( extension ) );
	}
}
