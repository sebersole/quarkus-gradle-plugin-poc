package com.github.sebersole.gradle.quarkus.dependency;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;
import java.util.function.Supplier;

import com.github.sebersole.gradle.quarkus.indexing.IndexAccess;

/**
 * Details related to a resolved dependency
 */
public interface ResolvedDependency extends ModuleVersionIdentifierAccess, Serializable {
	/**
	 * The base file/directory for the dependency.
	 *
	 * @apiNote The intention is that this be used for uniquely identifying the
	 * resolved dependency
	 */
	File getDependencyBase();

	Supplier<Properties> extensionMarkerPropertiesAccess();

	IndexAccess getIndexAccess();
}
