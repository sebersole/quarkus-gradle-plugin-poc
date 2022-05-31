package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.gradle.api.artifacts.Dependency;

/**
 * General helper
 */
public class Helper {
	public static final String QUARKUS = "quarkus";

	public static final String REPORT_BANNER_LINE = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
	public static final String REPORT_INDENTATION = "  ";

	public static final String QUARKUS_GROUP = "io.quarkus";
	public static final String QUARKUS_BOM = "quarkus-bom";
	public static final String QUARKUS_UNIVERSE_COMMUNITY_BOM = "quarkus-universe-bom";

	public static final String META_INF = "META-INF/";

	public static final String EXTENSION_PROP_FILE = META_INF + "quarkus-extension.properties";
	public static final String DEPLOYMENT_ARTIFACT_KEY = "deployment-artifact";

	public static final String JANDEX_FILE_NAME = "jandex.idx";
	public static final String JANDEX_INDEX_FILE_PATH = META_INF + JANDEX_FILE_NAME;

	public static String groupArtifactVersion(String group, String artifact, String version) {
		return String.format(
				Locale.ROOT,
				"%s:%s:%s",
				group,
				artifact,
				version
		);
	}

	public static String groupArtifactVersion(Dependency dependency) {
		return groupArtifactVersion( dependency.getGroup(), dependency.getName(), dependency.getVersion() );
	}

	public static void ensureFileExists(File file) {
		try {
			//noinspection ResultOfMethodCallIgnored
			file.getParentFile().mkdirs();
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

	@FunctionalInterface
	public interface Action {
		void execute();
	}
	public static <T> T extractOnlyOne(
			Collection<T> collection,
			Action missingAction,
			Action tooManyAction) {
		final Iterator<T> iterator = collection.iterator();

		if ( ! iterator.hasNext() ) {
			missingAction.execute();
//			if ( required ) {
//				throw new GradleException( "ResolvedDependency set was empty but expecting one value : " + extension.getName() );
//			}
			return null;
		}

		final T resolvedArtifactDependency = iterator.next();

		if ( iterator.hasNext() ) {
			tooManyAction.execute();
			return null;
		}

		return resolvedArtifactDependency;
	}
}
