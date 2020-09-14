package org.hibernate.build.gradle.quarkus.extension;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import org.gradle.api.Project;

import org.hibernate.build.gradle.quarkus.Helper;
import org.hibernate.build.gradle.quarkus.QuarkusConfigException;
import org.hibernate.build.gradle.quarkus.QuarkusDsl;

/**
 * Hibernate ORM specific "extension config" object
 *
 * @author Steve Ebersole
 */
public class HibernateOrmExtension extends AbstractExtension implements Serializable {
	public static final String CONTAINER_NAME = "hibernateOrm";

	public static final String ARTIFACT_ID = "hibernate-orm";

	public static final String PROP_PREFIX = "i.forget.the.quarkus.specific.prefix";
	public static final String FAMILY_PROP = PROP_PREFIX + ".db-family";
	public static final String URL_PROP = PROP_PREFIX + ".jdbc-url";
	public static final String USER_PROP = PROP_PREFIX + ".jdbc-user";
	public static final String PASS_PROP = PROP_PREFIX + ".jdbc-password";

	private SupportedDatabaseFamily appliedFamily;

	public HibernateOrmExtension(QuarkusDsl quarkusDsl) {
		super(
				CONTAINER_NAME,
				CONTAINER_NAME,
				(extension, quarkusDsl1) -> new Artifact(
						Helper.groupArtifactVersion(
								Helper.QUARKUS_GROUP,
								Helper.QUARKUS + "-" + ARTIFACT_ID,
								quarkusDsl.getQuarkusVersion()
						),
						extension,
						quarkusDsl
				),
				quarkusDsl
		);

		final Project project = quarkusDsl.getProject();

		project.afterEvaluate(
				p -> {
					if ( appliedFamily == null ) {
						throw new QuarkusConfigException( "No database-family was specified for hibernate-orm extension" );
					}

					final Extension extensionByGav = quarkusDsl.findExtensionByGav( appliedFamily.artifactId );
					if ( extensionByGav == null ) {
						// create and register one
						final TransitiveExtension transitiveExtension = new TransitiveExtension(
								appliedFamily.artifactId,
								Helper.groupArtifactVersion(
										Helper.QUARKUS_GROUP,
										appliedFamily.artifactId,
										quarkusDsl.getQuarkusVersion()
								),
								quarkusDsl
						);
						quarkusDsl.getModules().add( transitiveExtension );
						quarkusDsl.registerExtensionByGav( appliedFamily.artifactId, transitiveExtension );

						project.getDependencies().add(
								transitiveExtension.getDependencies().getName(),
								transitiveExtension.getArtifact().getDependencyNotation()
						);

						// force the dependency resolution
						transitiveExtension.getDependencies().getResolvedConfiguration();
					}
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
