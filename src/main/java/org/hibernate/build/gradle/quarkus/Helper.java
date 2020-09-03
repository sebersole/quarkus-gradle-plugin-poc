package org.hibernate.build.gradle.quarkus;

import java.util.Locale;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static final String QUARKUS = "quarkus";

	public static final String REPORT_BANNER_LINE = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
	public static final String REPORT_INDENTATION = "  ";
	public static final String REPORT_INDENTATION_MARKER = ">";

	public static String extensionName(String containerName) {
		return containerName.startsWith( "quarkus-" )
				? containerName
				: "quarkus-" + containerName;
	}

	public static String containerName(String extensionName) {
		assert extensionName.startsWith( "quarkus-" );

		return extensionName.substring( "quarkus-".length() );
	}

	public static String coordinate(String group, String artifact, String version) {
		return String.format(
				Locale.ROOT,
				"%s:%s:%s",
				group,
				artifact,
				version
		);
	}

	public static String moduleId(String group, String artifact) {
		return String.format(
				Locale.ROOT,
				"%s:%s",
				group,
				artifact
		);
	}

	private Helper() {
		// disallow direct instantiation
	}
}
