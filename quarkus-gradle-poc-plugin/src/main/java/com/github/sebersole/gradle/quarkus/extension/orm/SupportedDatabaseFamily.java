package com.github.sebersole.gradle.quarkus.extension.orm;

import java.util.Locale;

/**
 * @author Steve Ebersole
 */
enum SupportedDatabaseFamily {
	DERBY( "derby" ),
	H2( "h2" );

	private final String simpleName;
	private final String artifactId;
	private final String jdbcUrlProtocol;

	SupportedDatabaseFamily(String simpleName) {
		this.simpleName = simpleName;
		this.artifactId = "quarkus-jdbc-" + simpleName;
		this.jdbcUrlProtocol = "jdbc:" + simpleName + ":";
	}

	public static SupportedDatabaseFamily extractFromUrl(String url) {
		final SupportedDatabaseFamily[] families = SupportedDatabaseFamily.values();
		//noinspection ForLoopReplaceableByForEach
		for ( int i = 0; i < families.length; i++ ) {
			if ( url.startsWith( families[ i ].jdbcUrlProtocol ) ) {
				return families[ i ];
			}
		}

		return null;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getJdbcUrlProtocol() {
		return jdbcUrlProtocol;
	}

	public static SupportedDatabaseFamily fromName(String name) {
		if ( DERBY.name().equals( name.toUpperCase( Locale.ROOT ) ) ) {
			return DERBY;
		}

		if ( H2.name().equals( name.toUpperCase( Locale.ROOT ) ) ) {
			return H2;
		}

		return null;
	}
}
