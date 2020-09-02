package org.hibernate.build.gradle.quarkus.extension;

import java.util.Locale;
import java.util.Objects;

import org.gradle.api.Project;

import org.hibernate.build.gradle.quarkus.QuarkusBuildConfig;

/**
 * Hibernate ORM specific "extension config" object
 *
 * @author Steve Ebersole
 */
public class HibernateOrmExtensionConfig extends StandardExtensionConfig {
	public static final String PROP_PREFIX = "i.forget.the.quarkus.specific.prefix";
	public static final String FAMILY_PROP = PROP_PREFIX + ".db-family";
	public static final String URL_PROP = PROP_PREFIX + ".jdbc-url";
	public static final String USER_PROP = PROP_PREFIX + ".jdbc-user";
	public static final String PASS_PROP = PROP_PREFIX + ".jdbc-password";

	private final QuarkusBuildConfig quarkusBuildConfig;
	private final Project project;
	private final String deploymentConfigName;

	private SupportedDatabaseFamily appliedFamily;

	public HibernateOrmExtensionConfig(String name, QuarkusBuildConfig quarkusBuildConfig) {
		super( name );

		this.quarkusBuildConfig = quarkusBuildConfig;
		this.project = quarkusBuildConfig.getProject();
		this.deploymentConfigName = quarkusBuildConfig.getDeploymentConfiguration().getName();

		addModuleDependency( name );
	}

	private void addModuleDependency(String moduleName) {
		project.getLogger().lifecycle( "Adding Quarkus module dependency : %5", moduleName );

		final String gav = String.format(
				Locale.ROOT,
				"io.quarkus:%s:%s",
				moduleName,
				quarkusBuildConfig.getQuarkusVersion()
		);

		project.getDependencies().add( this.deploymentConfigName, gav );
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

			addModuleDependency( family.getQuarkusModuleName() );
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
}
