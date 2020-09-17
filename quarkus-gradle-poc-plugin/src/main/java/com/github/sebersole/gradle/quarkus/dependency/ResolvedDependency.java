package com.github.sebersole.gradle.quarkus.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;

import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.JarIndexer;

import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;

import static com.github.sebersole.gradle.quarkus.Helper.JANDEX_INDEX_FILE_PATH;

/**
 * 	Details related to a resolved dependency
 *
 * @author Steve Ebersole
 */
public class ResolvedDependency {

	private final String group;
	private final String artifact;
	private final String version;
	private final File artifactBase;
	private final IndexView jandexIndex;

	public ResolvedDependency(String group, String artifact, String version, File artifactBase, IndexView jandexIndex) {
		this.group = group;
		this.artifact = artifact;
		this.version = version;
		this.artifactBase = artifactBase;
		this.jandexIndex = jandexIndex;
	}

	/**
	 * The dependency's group-id (`org.hibernate.orm` e.g.)
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * The dependency's artifact-id (`hibernate-core` e.g.)
	 */
	public String getArtifact() {
		return artifact;
	}

	/**
	 * The dependency's version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @apiNote This could be an archive or a directory (for project dependencies)
	 */
	public File getArtifactBase() {
		return artifactBase;
	}

	/**
	 * The Jandex index derived from the artifact
	 */
	public IndexView getJandexIndex() {
		return jandexIndex;
	}
}
