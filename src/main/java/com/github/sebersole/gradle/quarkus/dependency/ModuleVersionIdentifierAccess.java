package com.github.sebersole.gradle.quarkus.dependency;

/**
 * Access to a ModuleIdentifier
 */
public interface ModuleVersionIdentifierAccess extends ModuleIdentifierAccess, ModuleVersionIdentifier {
	/**
	 * An identifier used in mapping a module (project or external dependency)
	 */
	ModuleVersionIdentifier getModuleVersionIdentifier();

	@Override
	default ModuleIdentifier getModuleIdentifier() {
		return getModuleVersionIdentifier();
	}

	@Override
	default String getVersion() {
		return getModuleVersionIdentifier().getVersion();
	}
}
