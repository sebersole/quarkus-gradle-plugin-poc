package com.github.sebersole.gradle.quarkus;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/**
 * @author Steve Ebersole
 */
public interface BuildDetails {
	Project getProject();
	String getQuarkusVersion();
	File getWorkingDirectory();
	Configuration getPlatforms();
	Configuration getRuntimeDependencies();
	Configuration getDeploymentDependencies();
}
