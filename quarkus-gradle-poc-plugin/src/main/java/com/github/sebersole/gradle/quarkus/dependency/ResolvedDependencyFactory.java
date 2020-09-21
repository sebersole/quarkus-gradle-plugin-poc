package com.github.sebersole.gradle.quarkus.dependency;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.indexing.IndexAccess;
import com.github.sebersole.gradle.quarkus.task.JandexTask;

import static com.github.sebersole.gradle.quarkus.Helper.groupArtifactVersion;

public class ResolvedDependencyFactory {
	public static ResolvedDependency from(ResolvedArtifact resolvedArtifact, QuarkusDslImpl quarkusDsl) {
		final String gav = groupArtifactVersion( resolvedArtifact );

		final IndexAccess indexAccess = JandexTask.apply(
				gav,
				resolvedArtifact,
				quarkusDsl
		);

		final ModuleVersionIdentifier moduleVersionIdentifier = resolvedArtifact.getModuleVersion().getId();
		return new ResolvedDependency(
				gav,
				moduleVersionIdentifier.getGroup(),
				moduleVersionIdentifier.getName(),
				moduleVersionIdentifier.getVersion(),
				indexAccess
		);
	}
}
