package org.hibernate.build.gradle.quarkus.extension;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import org.hibernate.build.gradle.quarkus.QuarkusConfigException;
import org.hibernate.build.gradle.quarkus.QuarkusDsl;

/**
 * Hibernate ORM specific "extension config" object
 *
 * @author Steve Ebersole
 */
public class HibernateOrmExtensionDsl extends StandardExtensionDsl implements Serializable {
	public static final String PROP_PREFIX = "i.forget.the.quarkus.specific.prefix";
	public static final String FAMILY_PROP = PROP_PREFIX + ".db-family";
	public static final String URL_PROP = PROP_PREFIX + ".jdbc-url";
	public static final String USER_PROP = PROP_PREFIX + ".jdbc-user";
	public static final String PASS_PROP = PROP_PREFIX + ".jdbc-password";

	private SupportedDatabaseFamily appliedFamily;

	public HibernateOrmExtensionDsl(ExtensionIdentifier identifier, Configuration dependencyConfiguration, QuarkusDsl quarkusDsl) {
		super( identifier, dependencyConfiguration, quarkusDsl );

		final Project project = quarkusDsl.getProject();

		project.afterEvaluate(
				p -> {
					if ( appliedFamily == null ) {
						throw new QuarkusConfigException( "No database-family was specified for hibernate-orm extension" );
					}

					final ExtensionIdentifier extensionIdentifier = ExtensionIdentifier.fromArtifactId( appliedFamily.artifactId, quarkusDsl );
					quarkusDsl.getModules().maybeCreate( extensionIdentifier.getDslContainerName() );
				}
		);
	}


	public void derby() {
		databaseFamily( "derby" );
	}

	public void mariadb() {
		databaseFamily( "mariadb" );
	}

	public void databaseFamily(String familyName) {
		applyDatabaseFamily( SupportedDatabaseFamily.fromName( familyName ) );
	}

	private void applyDatabaseFamily(SupportedDatabaseFamily family) {
		if ( Objects.equals( appliedFamily, family ) ) {
			return;
		}

		if ( family != null ) {
			applyProperty( FAMILY_PROP, family );
		}

		appliedFamily = family;
	}

	@Override
	public void property(Object key, Object value) {
		if ( URL_PROP.equals( key ) ) {
			jdbcUrl( (String) value );
		}
		else if ( USER_PROP.equals( key ) ) {
			jdbcUsername( (String) value );
		}
		else if ( PASS_PROP.equals( key ) ) {
			jdbcPassword( (String) value );
		}
		else {
			applyProperty( key, value );
		}
	}

	public void jdbcUrl(String url) {
		applyProperty( URL_PROP, url );

		final SupportedDatabaseFamily extractedFamily = SupportedDatabaseFamily.extractFromUrl( url );
		if ( extractedFamily != null ) {
			applyDatabaseFamily( extractedFamily );
		}
	}

	public void jdbcUsername(String userName) {
		applyProperty( USER_PROP, userName );
	}

	public void jdbcPassword(String userName) {
		applyProperty( PASS_PROP, userName );
	}

	enum SupportedDatabaseFamily {
		DERBY( "derby" ),
		H2( "h2" );

		private final String artifactId;
		private final String jdbcUrlProtocol;

		SupportedDatabaseFamily(String simpleName) {
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
}
