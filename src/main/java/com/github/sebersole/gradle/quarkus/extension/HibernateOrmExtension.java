package com.github.sebersole.gradle.quarkus.extension;

import java.io.Serializable;
import java.util.Locale;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusConfigException;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;

/**
 * Hibernate ORM specific "extension config" object
 *
 * @author Steve Ebersole
 */
public class HibernateOrmExtension extends AbstractExtension implements Serializable {
	public static final String CONTAINER_NAME = "hibernateOrm";
	public static final String ARTIFACT_SHORT_NAME = "hibernate-orm";

	private SupportedDatabaseFamily appliedFamily;

	public HibernateOrmExtension(QuarkusDsl quarkusDsl) {
		super(
				CONTAINER_NAME,
				(extension, quarkusDsl1) -> new Artifact(
						Helper.groupArtifactVersion(
								Helper.QUARKUS_GROUP,
								Helper.QUARKUS + "-" + ARTIFACT_SHORT_NAME,
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
						( (NamedDomainObjectContainer) quarkusDsl.getModules() ).add( transitiveExtension );
						quarkusDsl.registerExtensionByGav( appliedFamily.artifactId, transitiveExtension );

						project.getDependencies().add(
								transitiveExtension.getDependencies().getName(),
								transitiveExtension.getArtifact().getDependency()
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


	public String getDatabaseFamily() {
		return appliedFamily == null ? null : appliedFamily.name();
	}

	public void setDatabaseFamily(String familyName) {
		databaseFamily( familyName );
	}

	public void databaseFamily(String familyName) {
		appliedFamily = SupportedDatabaseFamily.fromName( familyName );
	}

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
