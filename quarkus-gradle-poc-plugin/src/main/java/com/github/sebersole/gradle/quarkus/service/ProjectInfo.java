package com.github.sebersole.gradle.quarkus.service;

import java.io.Serializable;

import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import com.github.sebersole.gradle.quarkus.dependency.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.dependency.ModuleVersionIdentifierAccess;
import com.github.sebersole.gradle.quarkus.dependency.StandardModuleVersionIdentifier;

/**
 * Details about a local Project
 *
 * @apiNote Using File here rather than Gradle's Directory / RegularFile
 * since those are for sure not Serializable.
 */
public class ProjectInfo implements ModuleVersionIdentifierAccess, ModuleVersionIdentifier, Serializable {

	// NOTE : I don't think this is really serializable because Gradle's Directory and
	//		SourceSet are not Serializable

	private final String path;
	private final ModuleVersionIdentifier moduleVersionIdentifier;
	private final Directory projectDirectory;
	private final SourceSet mainSourceSet;

	public ProjectInfo(
			String path,
			String groupName,
			String artifactName,
			String version,
			Directory projectDirectory,
			SourceSet mainSourceSet) {
		this.path = path;
		this.moduleVersionIdentifier = new StandardModuleVersionIdentifier( groupName, artifactName, version );
		this.projectDirectory = projectDirectory;
		this.mainSourceSet = mainSourceSet;
	}

	public ProjectInfo(
			String path,
			String groupName,
			String artifactName,
			String version,
			Directory projectDirectory,
			JavaPluginConvention javaPluginConvention) {
		this(
				path,
				groupName,
				artifactName,
				version,
				projectDirectory,
				javaPluginConvention == null ? null : javaPluginConvention.getSourceSets().getByName( SourceSet.MAIN_SOURCE_SET_NAME )
		);
	}

	public String getPath() {
		return path;
	}

	public Directory getProjectDirectory() {
		return projectDirectory;
	}

	public SourceSet getMainSourceSet() {
		return mainSourceSet;
	}

	@Override
	public ModuleVersionIdentifier getModuleVersionIdentifier() {
		return moduleVersionIdentifier;
	}
}
