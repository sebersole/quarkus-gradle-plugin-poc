package com.github.sebersole.gradle.quarkus.extension.orm;

import org.gradle.api.NamedDomainObjectFactory;

import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.extension.ExtensionFactorySupport;

/**
 * @author Steve Ebersole
 */
public class HibernateOrmExtensionFactory implements NamedDomainObjectFactory<HibernateOrmExtension> {
	private final QuarkusDslImpl quarkusDsl;
	private HibernateOrmExtension singleton;

	public HibernateOrmExtensionFactory(QuarkusDslImpl quarkusDsl) {
		this.quarkusDsl = quarkusDsl;
	}

	@Override
	public HibernateOrmExtension create(String dslName) {
		if ( singleton == null ) {
			singleton = new HibernateOrmExtension( quarkusDsl );

			ExtensionFactorySupport.prepareExtension( singleton, quarkusDsl );
		}

		return singleton;
	}
}
