package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.Action;

import groovy.lang.Closure;

/**
 * Access to shortcuts for creating Extension references
 *
 * @author Steve Ebersole
 */
public interface ExtensionCreationShortCuts {
	Extension extension(String name);
	Extension extension(String name, Closure<Extension> closure);
	Extension extension(String name, Action<Extension> action);

	Extension quarkusExtension(String shortName);
	Extension quarkusExtension(String shortName, Closure<Extension> closure);
	Extension quarkusExtension(String shortName, Action<Extension> action);

	Extension quarkusExtension(String dslName, String shortName);
	Extension quarkusExtension(String dslName, String shortName, Closure<Extension> closure);
	Extension quarkusExtension(String dslName, String shortName, Action<Extension> action);

	HibernateOrmExtension hibernateOrm();

	HibernateOrmExtension hibernateOrm(String databaseFamilyName);

	HibernateOrmExtension hibernateOrm(Closure<HibernateOrmExtension> closure);

	HibernateOrmExtension hibernateOrm(Action<HibernateOrmExtension> action);
}
