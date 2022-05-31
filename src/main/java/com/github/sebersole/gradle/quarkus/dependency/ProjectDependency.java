package com.github.sebersole.gradle.quarkus.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.service.ProjectInfo;
import com.github.sebersole.gradle.quarkus.indexing.IndexAccess;

/**
 * ResolvedDependency for Project dependencies.
 */
public class ProjectDependency implements ResolvedDependency, ModuleVersionIdentifier {
	private final ProjectInfo projectInfo;
	private final IndexAccess indexAccess;

	private Properties extensionProperties;

	public ProjectDependency(
			ProjectInfo projectInfo,
			Function<ResolvedDependency, IndexAccess> indexAccessCreator) {
		this.projectInfo = projectInfo;
		this.indexAccess = indexAccessCreator.apply( this );
	}

	public String getProjectPath() {
		return projectInfo.getPath();
	}

	public Directory getProjectDirectory() {
		return projectInfo.getProjectDirectory();
	}

	@Override
	public ModuleVersionIdentifier getModuleVersionIdentifier() {
		return projectInfo;
	}

	@Override
	public File getDependencyBase() {
		// this is the file/dir used to index the project as a dependency
		// not the "indexing base"
		return projectInfo.getProjectDirectory().getAsFile();
	}

	@Override
	public Supplier<Properties> extensionMarkerPropertiesAccess() {
		return this::resolveExtensionProperties;
	}

	private Properties resolveExtensionProperties() {
		if ( extensionProperties != null ) {
			return extensionProperties;
		}

		extensionProperties = readExtensionProperties( projectInfo );
		return extensionProperties;
	}

	private static Properties readExtensionProperties(ProjectInfo projectInfo) {
		final Properties properties = new Properties();

		final File outputDir = projectInfo.getMainSourceSet().getResources().getOutputDir();
		if ( outputDir.exists() ) {
			final File propFile = new File( outputDir, Helper.EXTENSION_PROP_FILE );
			if ( propFile.exists() ) {
				try ( final FileInputStream propStream = new FileInputStream( propFile ) ) {
					properties.load( propStream );
				}
				catch (FileNotFoundException e) {
					// this should *never* happen since we've already checked above...
					throw new GradleException( "FileNotFoundException trying to read Quarkus extension properties file", e );
				}
				catch (Exception e) {
					throw new GradleException( "Error accessing the Quarkus extension properties file", e );
				}
			}
		}

		return properties;
	}

	@Override
	public IndexAccess getIndexAccess() {
		return indexAccess;
	}
}
