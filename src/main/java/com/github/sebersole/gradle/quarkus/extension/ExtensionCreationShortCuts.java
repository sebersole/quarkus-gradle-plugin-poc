package com.github.sebersole.gradle.quarkus.extension;

import java.util.function.BiConsumer;

import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;
import groovy.lang.Closure;

/**
 * Access to shortcuts for creating Extension references
 *
 * @author Steve Ebersole
 */
public interface ExtensionCreationShortCuts {
	QuarkusDsl getQuarkusDsl();
	BiConsumer<Extension, QuarkusDsl> getCreatedExtensionPreparer();

	default Extension extension(String name, Closure<Extension> closure) {
		return getQuarkusDsl().getModules().create( name, closure );
	}

	default Extension extension(String name, Action<Extension> action) {
		return getQuarkusDsl().getModules().create( name, action );
	}

	default Extension quarkusExtension(String shortName) {
		final Extension extension = getQuarkusDsl().getModules().create( shortName );
		extension.artifact(
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						Helper.QUARKUS + "-" + shortName,
						getQuarkusDsl().getQuarkusVersion()
				)
		);
		return extension;
	}

	default Extension quarkusExtension(String dslName, String shortName) {
		final Extension extension = getQuarkusDsl().getModules().create( dslName );
		extension.artifact(
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						Helper.QUARKUS + "-" + shortName,
						getQuarkusDsl().getQuarkusVersion()
				)
		);
		return extension;
	}

	default Extension quarkusExtension(String shortName, Closure<Extension> closure) {
		final Extension extension = getQuarkusDsl().getModules().create( shortName, closure );
		extension.artifact(
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						Helper.QUARKUS + "-" + shortName,
						getQuarkusDsl().getQuarkusVersion()
				)
		);
		return extension;
	}

	default Extension quarkusExtension(String dslName, String shortName, Closure<Extension> closure) {
		final Extension extension = getQuarkusDsl().getModules().create( dslName, closure );
		extension.artifact(
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						Helper.QUARKUS + "-" + shortName,
						getQuarkusDsl().getQuarkusVersion()
				)
		);
		return extension;
	}

	default Extension quarkusExtension(String shortName, Action<Extension> action) {
		final Extension extension = getQuarkusDsl().getModules().create( shortName );
		extension.artifact(
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						Helper.QUARKUS + "-" + shortName,
						getQuarkusDsl().getQuarkusVersion()
				)
		);
		action.execute( extension );
		return extension;
	}

	default Extension quarkusExtension(String dslName, String shortName, Action<Extension> action) {
		final Extension extension = getQuarkusDsl().getModules().create( dslName );
		extension.artifact(
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						Helper.QUARKUS + "-" + shortName,
						getQuarkusDsl().getQuarkusVersion()
				)
		);
		action.execute( extension );
		return extension;
	}

	default HibernateOrmExtension hibernateOrm() {
		final QuarkusDsl quarkusDsl = getQuarkusDsl();
		final HibernateOrmExtension ormExtension = new HibernateOrmExtension( quarkusDsl );
		quarkusDsl.getModules().add( ormExtension );
		getCreatedExtensionPreparer().accept( ormExtension, quarkusDsl );
		return ormExtension;
	}

	default HibernateOrmExtension hibernateOrm(String databaseFamilyName) {
		final QuarkusDsl quarkusDsl = getQuarkusDsl();
		final HibernateOrmExtension ormExtension = new HibernateOrmExtension( quarkusDsl );
		ormExtension.databaseFamily( databaseFamilyName );
		quarkusDsl.getModules().add( ormExtension );
		getCreatedExtensionPreparer().accept( ormExtension, quarkusDsl );
		return ormExtension;
	}

	default HibernateOrmExtension hibernateOrm(Closure<HibernateOrmExtension> closure) {
		final QuarkusDsl quarkusDsl = getQuarkusDsl();
		final HibernateOrmExtension ormExtension = new HibernateOrmExtension( quarkusDsl );
		ConfigureUtil.configure( closure, ormExtension );
		quarkusDsl.getModules().add( ormExtension );
		getCreatedExtensionPreparer().accept( ormExtension, quarkusDsl );
		return ormExtension;
	}

	default HibernateOrmExtension hibernateOrm(Action<HibernateOrmExtension> action) {
		final QuarkusDsl quarkusDsl = getQuarkusDsl();
		final HibernateOrmExtension ormExtension = new HibernateOrmExtension( quarkusDsl );
		action.execute( ormExtension );
		quarkusDsl.getModules().add( ormExtension );
		getCreatedExtensionPreparer().accept( ormExtension, quarkusDsl );
		return ormExtension;
	}
}
