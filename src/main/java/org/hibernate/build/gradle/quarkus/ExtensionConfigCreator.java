package org.hibernate.build.gradle.quarkus;

import java.io.Serializable;
import java.util.Locale;

import org.gradle.api.NamedDomainObjectFactory;

import org.hibernate.build.gradle.quarkus.extension.ExtensionDsl;
import org.hibernate.build.gradle.quarkus.extension.HibernateOrmExtensionDsl;
import org.hibernate.build.gradle.quarkus.extension.StandardExtensionDsl;

/**
 * Factory for ExtensionConfig references
 *
 * @author Steve Ebersole
 */
public class ExtensionConfigCreator implements NamedDomainObjectFactory<ExtensionDsl>, Serializable {
	private final QuarkusDsl quarkusDsl;
	private final String deploymentConfigName;

	public ExtensionConfigCreator(QuarkusDsl quarkusDsl) {
		this.quarkusDsl = quarkusDsl;
		this.deploymentConfigName = quarkusDsl.getDeploymentConfiguration().getName();
	}

	@Override
	public ExtensionDsl create(String name) {
		final String moduleName = name.startsWith( "quarkus-" )
				? name
				: "quarkus-" + name;

		final String gav = String.format(
				Locale.ROOT,
				"io.quarkus:%s:%s",
				moduleName,
				quarkusDsl.getQuarkusVersion()
		);

		quarkusDsl.getProject().getDependencies().add( deploymentConfigName, gav );

		if ( "quarkus-hibernate-orm".equals( moduleName ) ) {
			return new HibernateOrmExtensionDsl( moduleName, quarkusDsl );
		}

		return new StandardExtensionDsl( moduleName );
	}
}
