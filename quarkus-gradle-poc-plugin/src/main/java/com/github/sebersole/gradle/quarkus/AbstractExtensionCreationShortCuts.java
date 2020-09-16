package com.github.sebersole.gradle.quarkus;

import java.util.function.BiConsumer;

import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.extension.ExtensionCreationShortCuts;
import com.github.sebersole.gradle.quarkus.extension.HibernateOrmExtension;
import groovy.lang.Closure;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractExtensionCreationShortCuts implements ExtensionCreationShortCuts {
	protected abstract QuarkusDslImpl getQuarkusDsl();
	protected abstract BiConsumer<Extension, QuarkusDsl> getCreatedExtensionPreparer();

	@Override
	public Extension extension(String name) {
		return getQuarkusDsl().getQuarkusExtensions().create( name );
	}

	@Override
	public Extension extension(String name, Closure<Extension> closure) {
		return getQuarkusDsl().getQuarkusExtensions().create( name, closure );
	}

	@Override
	public Extension extension(String name, Action<Extension> action) {
		return getQuarkusDsl().getQuarkusExtensions().create( name, action );
	}

	@Override
	public Extension quarkusExtension(String shortName) {
		return extension(
				shortName,
				created -> applyArtifact( shortName, created )
		);
	}

	@Override
	public Extension quarkusExtension(String dslName, String shortName) {
		return extension(
				dslName,
				created -> applyArtifact( shortName, created )
		);
	}

	private void applyArtifact(String shortName, Extension created) {
		created.artifact(
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						Helper.QUARKUS + "-" + shortName,
						getQuarkusDsl().getQuarkusVersion()
				)
		);
	}

	@Override
	public Extension quarkusExtension(String shortName, Closure<Extension> closure) {
		final Extension extension = quarkusExtension( shortName );
		ConfigureUtil.configure( closure, extension );
		return extension;
	}

	@Override
	public Extension quarkusExtension(String dslName, String shortName, Closure<Extension> closure) {
		final Extension extension = quarkusExtension( dslName, shortName );
		ConfigureUtil.configure( closure, extension );
		return extension;
	}

	@Override
	public Extension quarkusExtension(String shortName, Action<Extension> action) {
		final Extension extension = quarkusExtension( shortName );
		action.execute( extension );
		return extension;
	}

	@Override
	public Extension quarkusExtension(String dslName, String shortName, Action<Extension> action) {
		final Extension extension = quarkusExtension( dslName, shortName );
		action.execute( extension );
		return extension;
	}

	@Override
	public HibernateOrmExtension hibernateOrm() {
		final HibernateOrmExtension ormExtension = new HibernateOrmExtension( getQuarkusDsl() );
		getQuarkusDsl().getQuarkusExtensions().add( ormExtension );
		getCreatedExtensionPreparer().accept( ormExtension, getQuarkusDsl() );
		return ormExtension;
	}

	@Override
	public HibernateOrmExtension hibernateOrm(String databaseFamilyName) {
		final HibernateOrmExtension ormExtension = new HibernateOrmExtension( getQuarkusDsl() );
		ormExtension.databaseFamily( databaseFamilyName );
		getQuarkusDsl().getQuarkusExtensions().add( ormExtension );
		getCreatedExtensionPreparer().accept( ormExtension, getQuarkusDsl() );
		return ormExtension;
	}

	@Override
	public HibernateOrmExtension hibernateOrm(Closure<HibernateOrmExtension> closure) {
		final HibernateOrmExtension ormExtension = new HibernateOrmExtension( getQuarkusDsl() );
		ConfigureUtil.configure( closure, ormExtension );
		getQuarkusDsl().getQuarkusExtensions().add( ormExtension );
		getCreatedExtensionPreparer().accept( ormExtension, getQuarkusDsl() );
		return ormExtension;
	}

	@Override
	public HibernateOrmExtension hibernateOrm(Action<HibernateOrmExtension> action) {
		final QuarkusDslImpl quarkusDsl = getQuarkusDsl();
		final HibernateOrmExtension ormExtension = new HibernateOrmExtension( quarkusDsl );
		action.execute( ormExtension );
		quarkusDsl.getQuarkusExtensions().add( ormExtension );
		getCreatedExtensionPreparer().accept( ormExtension, quarkusDsl );
		return ormExtension;
	}
}
