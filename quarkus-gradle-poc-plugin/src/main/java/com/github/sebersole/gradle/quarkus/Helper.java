package com.github.sebersole.gradle.quarkus;

import java.util.Locale;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static final String QUARKUS = "quarkus";

	public static final String REPORT_BANNER_LINE = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
	public static final String REPORT_INDENTATION = "  ";
	public static final String REPORT_INDENTATION_MARKER = ">";

	public static final String QUARKUS_GROUP = "io.quarkus";
	public static final String QUARKUS_BOM = "quarkus-bom";
	public static final String QUARKUS_UNIVERSE_COMMUNITY_BOM = "quarkus-universe-bom";
	public static final String EXTENSION_MARKER_FILE = "META-INF/quarkus-extension.properties";

	public static <T> T instantiate(Class<T> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		}
		catch (Exception e) {
			throw new GradleException( "Unable to instantiate specified Extension type : " + type.getName() );
		}
	}

	public static String groupArtifact(String group, String artifact) {
		return String.format(
				Locale.ROOT,
				"%s:%s",
				group,
				artifact
		);
	}

	public static String groupArtifactVersion(String group, String artifact, String version) {
		return String.format(
				Locale.ROOT,
				"%s:%s:%s",
				group,
				artifact,
				version
		);
	}

	public static String groupArtifactVersion(ResolvedArtifact resolvedArtifact) {
		final ModuleVersionIdentifier identifier = resolvedArtifact.getModuleVersion().getId();
		return groupArtifactVersion( identifier.getGroup(), identifier.getName(), identifier.getVersion() );
	}

	public static String groupArtifactVersion(Dependency dependency) {
		return groupArtifactVersion( dependency.getGroup(), dependency.getName(), dependency.getVersion() );
	}

	private Helper() {
		// disallow direct instantiation
	}
}
