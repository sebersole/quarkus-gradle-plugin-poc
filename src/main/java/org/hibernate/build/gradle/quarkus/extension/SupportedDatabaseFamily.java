package org.hibernate.build.gradle.quarkus.extension;

import java.util.Locale;

/**
 * @author Steve Ebersole
 */
enum SupportedDatabaseFamily {
	// supported families
	DERBY( "i-forget-derby", "derby" ),
	H2( "i-forget-h2", "h2" );

	private final String quarkusModuleName;
	private final String jdbcUrlProtocol;

	SupportedDatabaseFamily(String quarkusModuleName, String jdbcUrlSubProtocol) {
		this.quarkusModuleName = quarkusModuleName;
		this.jdbcUrlProtocol = "jdbc:" + jdbcUrlSubProtocol + ":";
	}

	public String getQuarkusModuleName() {
		return quarkusModuleName;
	}

	public static SupportedDatabaseFamily extractFromUrl(String url) {
		final SupportedDatabaseFamily[] families = values();
		for ( int i = 0; i < families.length; i++ ) {
			if ( url.startsWith( families[ i ].jdbcUrlProtocol ) ) {
				return families[ i ];
			}
		}

		return null;
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
