package org.hibernate.build.gradle.quarkus.extension;

/**
 * @author Steve Ebersole
 */
@FunctionalInterface
public interface ExtensionModuleCreationListener {
	void extensionModuleCreated(Extension extension);
}
