package com.github.sebersole.gradle.quarkus.dependency;

import java.io.File;
import java.util.Objects;

import org.jboss.jandex.IndexView;

import com.github.sebersole.gradle.quarkus.Helper;

/**
 * Details related to a resolved dependency
 */
public class ResolvedDependency {

	// todo : make reading/creating and writing of the Jandex index delayed
	//		- `Supplier<IndexView>` e.g.

	private final String gav;

	private final String group;
	private final String artifact;
	private final String version;
	private final File artifactBase;
	private final IndexView jandexIndex;

	public ResolvedDependency(
			String group,
			String artifact,
			String version,
			File artifactBase,
			IndexView jandexIndex) {
		this.gav = Helper.groupArtifactVersion( group, artifact, version );

		this.group = group;
		this.artifact = artifact;
		this.version = version;
		this.artifactBase = artifactBase;
		this.jandexIndex = jandexIndex;
	}

	public String getGav() {
		return gav;
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

	@Override
	public String toString() {
		return "ResolvedDependency(`" + gav + "`)";
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;
		ResolvedDependency that = (ResolvedDependency) o;
		return gav.equals( that.gav );
	}

	@Override
	public int hashCode() {
		return Objects.hash( gav );
	}
}
