package com.github.sebersole.gradle.quarkus.dependency;

import java.util.Locale;
import java.util.Objects;

import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedModuleVersion;

/**
 * Basic implementation of the ModuleIdentifier contract
 */
public class StandardModuleVersionIdentifier implements ModuleVersionIdentifier {
	private final String groupName;
	private final String artifactName;
	private final String version;

	public StandardModuleVersionIdentifier(String groupName, String artifactName, String version) {
		this.groupName = groupName;
		this.artifactName = artifactName;
		this.version = version;
	}

	public StandardModuleVersionIdentifier(ResolvedArtifact gradleResolvedArtifact) {
		this( gradleResolvedArtifact.getModuleVersion() );
	}

	public StandardModuleVersionIdentifier(ResolvedModuleVersion moduleVersion) {
		this( moduleVersion.getId().getGroup(), moduleVersion.getId().getName(), moduleVersion.getId().getVersion() );
	}

	@Override
	public String getGroupName() {
		return groupName;
	}

	@Override
	public String getArtifactName() {
		return artifactName;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}

		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		final ModuleVersionIdentifier that = (ModuleVersionIdentifier) o;
		return groupName.equals( that.getGroupName() )
				&& artifactName.equals( that.getArtifactName() )
				&& version.equals( that.getVersion() );
	}

	@Override
	public int hashCode() {
		return Objects.hash( groupName, artifactName, version );
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"%s:%s:%s",
				groupName,
				artifactName,
				version
		);
	}
}
