package com.github.sebersole.gradle.quarkus.dependency;

import java.util.Objects;

import org.gradle.api.provider.Provider;

import org.jboss.jandex.IndexView;

/**
 * Details related to a resolved dependency
 */
public class ResolvedDependency {
	private final String gav;

	private final String group;
	private final String artifact;
	private final String version;

	private final Provider<IndexView> indexAccess;

	public ResolvedDependency(
			String gav,
			String group,
			String artifact,
			String version,
			Provider<IndexView> indexAccess) {
		this.gav = gav;

		this.group = group;
		this.artifact = artifact;
		this.version = version;

		this.indexAccess = indexAccess;
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
	 * Access to the Jandex index
	 */
	public Provider<IndexView> getJandexIndexAccess() {
		return indexAccess;
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
