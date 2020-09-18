package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;

import org.jboss.jandex.DotName;

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

	public static final String META_INF = "META-INF/";

	public static final String EXTENSION_YAML_FILE = META_INF + "quarkus-extension.yaml";
	public static final String EXTENSION_PROP_FILE = META_INF + "quarkus-extension.properties";

	public static final String JANDEX_FILE_NAME = "jandex.idx";
	public static final String JANDEX_INDEX_FILE_PATH = META_INF + JANDEX_FILE_NAME;

	public static DotName createJandexName(String... parts) {
		assert parts != null;
		assert parts.length > 0;

		DotName result = DotName.createComponentized( null, parts[0] );

		for ( int i = 1; i < parts.length; i++ ) {
			result = DotName.createComponentized( result, parts[i] );
		}

		return result;
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
		return groupArtifactVersion( identifier );
	}

	public static String groupArtifactVersion(ModuleVersionIdentifier identifier) {
		return groupArtifactVersion( identifier.getGroup(), identifier.getName(), identifier.getVersion() );
	}

	public static String groupArtifactVersion(Dependency dependency) {
		return groupArtifactVersion( dependency.getGroup(), dependency.getName(), dependency.getVersion() );
	}

	public static void ensureFileExists(File file, QuarkusDsl quarkusDsl) {
		try {
			//noinspection ResultOfMethodCallIgnored
			file.mkdirs();
			final boolean created = file.createNewFile();
			if ( created ) {
				return;
			}
		}
		catch (IOException e) {
			Logging.LOGGER.debug( "Unable to create file {} : {}", file.getAbsolutePath(), e.getMessage() );
			return;
		}

		Logging.LOGGER.debug( "Unable to ensure File existence {}", file.getAbsolutePath() );
	}

	private Helper() {
		// disallow direct instantiation
	}
}
