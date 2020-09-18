package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.io.Serializable;
import java.util.function.Consumer;

import org.gradle.api.Action;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.extension.ExtensionCreationShortCuts;
import groovy.lang.Closure;

/**
 * Gradle DSL extension for configuring the plugin - the public view
 */
public interface QuarkusDsl extends ExtensionCreationShortCuts, Serializable {
	Project getProject();

	String getQuarkusVersion();

	void setQuarkusVersion(String quarkusVersion);

	void quarkusVersion(String quarkusVersion);

	File getWorkingDir();

	void setWorkingDir(Object workingDir);

	void workingDir(Object workingDir);

	String getTestProfile();

	void setTestProfile(String testProfile);

	void testProfile(String testProfile);

	NativeArguments getNativeArgs();

	void nativeArgs(Closure<NativeArguments> configurer);

	void nativeArgs(Action<NativeArguments> configurer);

	void platform(String gav);

	void platform(String gav, Closure<Dependency> closure);

	void platform(String gav, Action<Dependency> action);

	void extensions(Closure<PolymorphicDomainObjectContainer<Extension>> extensionClosure);
	void extensions(Action<PolymorphicDomainObjectContainer<Extension>> action);

	void quarkusExtensions(Closure<PolymorphicDomainObjectContainer<Extension>> extensionClosure);

	void visitExtensions(Consumer<Extension> consumer);

	Configuration getRuntimeDependencies();
	Configuration getDeploymentDependencies();
}
