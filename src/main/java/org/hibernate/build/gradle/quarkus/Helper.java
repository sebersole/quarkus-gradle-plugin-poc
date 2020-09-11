package org.hibernate.build.gradle.quarkus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.gradle.api.GradleException;

import org.hibernate.build.gradle.quarkus.extension.ExtensionIdentifier;

import static java.util.Arrays.asList;

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

	public static Set<String> setOf(String... artifactIds) {
		return new HashSet<>( asList( artifactIds ) );
	}

	public static Map<String,String> mapOf(String... keysAndValues) {
		if ( keysAndValues.length % 2 != 0 ) {
			throw new GradleException( "Expecting even number of values to create Map" );
		}

		final HashMap<String,String> map = new HashMap<>();
		for ( int i = 0; i < keysAndValues.length; i+=2 ) {
			map.put( keysAndValues[i], keysAndValues[i+1] );
		}

		return map;
	}
}
