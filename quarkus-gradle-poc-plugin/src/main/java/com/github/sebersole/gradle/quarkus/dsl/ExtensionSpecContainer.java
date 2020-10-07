package com.github.sebersole.gradle.quarkus.dsl;

import java.util.Locale;

import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;
import groovy.lang.Closure;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS_GROUP;

/**
 * Container for the ExtensionSpec container :)
 *
 * It wraps:<ul>
 *     <li>
 *         a Gradle {@link org.gradle.api.ExtensiblePolymorphicDomainObjectContainer}
 *         containing the named ExtensionSpec references for the explicitly configured extensions
 *     </li>
 *     <li>
 *         methods providing creation for "well known" extensions.  for the moment, since hibernate-orm
 *         is the only specially recognized extension in terms of
 *     </li>
 * </ul>
 */
public class ExtensionSpecContainer {
	private final BuildDetails buildDetails;
	private final ExtensiblePolymorphicDomainObjectContainer<ExtensionSpec> extensionsContainer;

	@SuppressWarnings( "UnstableApiUsage" )
	public ExtensionSpecContainer(BuildDetails buildDetails) {
		this.buildDetails = buildDetails;
		this.extensionsContainer = buildDetails.getMainProject().getObjects().polymorphicDomainObjectContainer( ExtensionSpec.class );

		// Hibernate ORM specific factory
		extensionsContainer.registerFactory(
				HibernateOrmExtensionSpec.class,
				new HibernateOrmExtensionSpec.Factory( buildDetails )
		);

		extensionsContainer.registerFactory(
				ExtensionSpec.class,
				name -> new StandardExtensionSpec( name, buildDetails )
		);

		// default factory...
		extensionsContainer.registerFactory(
				StandardExtensionSpec.class,
				name -> new StandardExtensionSpec( name, buildDetails )
		);
	}

	public ExtensiblePolymorphicDomainObjectContainer<ExtensionSpec> getContainer() {
		return extensionsContainer;
	}

	public HibernateOrmExtensionSpec hibernateOrm() {
		return extensionsContainer.maybeCreate(
				HibernateOrmExtensionSpec.CONTAINER_NAME,
				HibernateOrmExtensionSpec.class
		);
	}

	public void hibernateOrm(Closure<HibernateOrmExtensionSpec> closure) {
		final HibernateOrmExtensionSpec ormExtension = hibernateOrm();
		ConfigureUtil.configure( closure, ormExtension );
	}

	public void hibernateOrm(Action<HibernateOrmExtensionSpec> action) {
		final HibernateOrmExtensionSpec ormExtension = hibernateOrm();
		action.execute( ormExtension );
	}

	public ExtensionSpec quarkusExtension(String shortName) {
		return quarkusExtension( shortName, shortName );
	}

	public <T extends ExtensionSpec> T quarkusExtension(String dslName, String shortName) {
		final ExtensionSpec extension = extensionsContainer.maybeCreate( dslName );
		quarkusArtifact( extension, shortName, buildDetails );

		//noinspection unchecked
		return (T) extension;
	}

	public <T extends ExtensionSpec> T quarkusExtension(String shortName, Class<T> specType) {
		return quarkusExtension( shortName, shortName, specType );
	}

	public <T extends ExtensionSpec> T quarkusExtension(String dslName, String shortName, Class<T> specType) {
		final T extension = extensionsContainer.maybeCreate( dslName, specType );
		quarkusArtifact( extension, shortName, buildDetails );

		return extension;
	}

	private static void quarkusArtifact(ExtensionSpec extension, String shortName, BuildDetails buildDetails) {
		final String dependencyId = String.format(
				Locale.ROOT,
				"%s:%s:%s",
				QUARKUS_GROUP,
				QUARKUS + "-" + shortName,
				buildDetails.getQuarkusVersion()
		);

		extension.artifact( dependencyId );
	}

	public <T extends ExtensionSpec> T quarkusExtension(String dslName, String shortName, Class<T> specType, Action<T> action) {
		final T extension = extensionsContainer.maybeCreate( dslName, specType );
		quarkusArtifact( extension, shortName, buildDetails );
		action.execute( extension );
		return extension;
	}

	public <T extends ExtensionSpec> T quarkusExtension(String dslName, String shortName, Class<T> specType, Closure closure) {
		final T extension = extensionsContainer.maybeCreate( dslName, specType );
		quarkusArtifact( extension, shortName, buildDetails );
		ConfigureUtil.configure( closure, extension );
		return extension;
	}
}
