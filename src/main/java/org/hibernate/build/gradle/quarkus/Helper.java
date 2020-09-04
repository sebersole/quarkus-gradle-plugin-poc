package org.hibernate.build.gradle.quarkus;

import java.util.Locale;

import org.hibernate.build.gradle.quarkus.extension.ExtensionIdentifier;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static final String QUARKUS = "quarkus";

	public static final String REPORT_BANNER_LINE = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
	public static final String REPORT_INDENTATION = "  ";
	public static final String REPORT_INDENTATION_MARKER = ">";

	public static final String QUARKUS_GROUP = "io.quarkus";

	public static String quarkusExtensionCoordinates(ExtensionIdentifier id, QuarkusDsl quarkusDsl) {
		return groupArtifactVersion( QUARKUS_GROUP, id.getQuarkusArtifactId(), quarkusDsl.getQuarkusVersion() );
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

	private Helper() {
		// disallow direct instantiation
	}
}
