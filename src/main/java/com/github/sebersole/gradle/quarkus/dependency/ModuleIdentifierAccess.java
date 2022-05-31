package com.github.sebersole.gradle.quarkus.dependency;

/**
 * Access to a ModuleIdentifier
 */
public interface ModuleIdentifierAccess extends ModuleIdentifier {
	/**
	 * Access the ModuleIdentifier
	 */
	ModuleIdentifier getModuleIdentifier();

	@Override
	default String getGroupName() {
		return getModuleIdentifier().getGroupName();
	}

	@Override
	default String getArtifactName() {
		return getModuleIdentifier().getArtifactName();
	}
}
