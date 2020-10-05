package com.github.sebersole.gradle.quarkus.dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.gradle.api.GradleException;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.indexing.IndexAccess;

/**
 * Resolved representation of an external dependency
 */
public class ExternalDependency implements ResolvedDependency, Serializable {
	private final ModuleVersionIdentifier moduleVersionIdentifier;
	private final File dependencyArtifactFile;
	private final IndexAccess indexAccess;

	private Properties extensionProperties;

	public ExternalDependency(
			ModuleVersionIdentifier moduleVersionIdentifier,
			File dependencyArtifactFile,
			Function<ResolvedDependency, IndexAccess> indexAccessCreator) {
		this.moduleVersionIdentifier = moduleVersionIdentifier;
		this.dependencyArtifactFile = dependencyArtifactFile;
		this.indexAccess = indexAccessCreator.apply( this );
	}

	@Override
	public ModuleVersionIdentifier getModuleVersionIdentifier() {
		return moduleVersionIdentifier;
	}

	@Override
	public File getDependencyBase() {
		return dependencyArtifactFile;
	}

	@Override
	public Supplier<Properties> extensionMarkerPropertiesAccess() {
		return this::resolveExtensionProperties;
	}

	private Properties resolveExtensionProperties() {
		if ( extensionProperties == null ) {
			extensionProperties = readExtensionProperties( dependencyArtifactFile );
		}

		return extensionProperties;
	}

	private static Properties readExtensionProperties(File dependencyArtifactFile) {
		try {
			final JarFile jarFile = new JarFile( dependencyArtifactFile );
			return readExtensionProperties( jarFile );
		}
		catch (IOException e) {
			throw new GradleException( "Unable to treat " );
		}
	}

	private static Properties readExtensionProperties(JarFile jarFile) {
		final Properties properties = new Properties();

		final ZipEntry entry = jarFile.getEntry( Helper.EXTENSION_PROP_FILE );
		if ( entry == null ) {
			return properties;
		}

		try ( final InputStream propsStream = jarFile.getInputStream( entry ) ) {
			properties.load( propsStream );
			return properties;
		}
		catch (IOException e) {
			throw new GradleException( "Error accessing the Quarkus extension properties file", e );
		}
	}


	@Override
	public IndexAccess getIndexAccess() {
		return indexAccess;
	}
}
