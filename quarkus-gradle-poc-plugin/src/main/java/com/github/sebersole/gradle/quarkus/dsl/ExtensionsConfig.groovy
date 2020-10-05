package com.github.sebersole.gradle.quarkus.dsl

import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.util.ConfigureUtil

import com.github.sebersole.gradle.quarkus.service.BuildDetails
import com.github.sebersole.gradle.quarkus.Helper
import com.github.sebersole.gradle.quarkus.dependency.ModuleVersionIdentifier
import com.github.sebersole.gradle.quarkus.dependency.StandardModuleVersionIdentifier

/**
 * Models the Quarkus `extensions {}` block - in other words, all explicitly declared extensions.
 *
 * Provides access to:
 * 		1) container of explicitly declared extensions
 * 		2) extension creation capabilities for "well known" extensions
 */
class ExtensionsConfig {
	private final BuildDetails buildDetails
	private final ExtensiblePolymorphicDomainObjectContainer<ExtensionConfig> extensionsContainer

	ExtensionsConfig(BuildDetails buildDetails) {
		this.buildDetails = buildDetails
		this.extensionsContainer = buildDetails.mainProject.objects.polymorphicDomainObjectContainer( ExtensionConfig.class )

		// Hibernate ORM specific factory
		extensionsContainer.registerFactory(
				HibernateOrmExtensionConfig.class,
				new HibernateOrmExtensionConfig.Factory( buildDetails )
		)

		// default factory...
		extensionsContainer.registerFactory(
				ExtensionConfig.class,
				new NamedDomainObjectFactory<ExtensionConfig>() {
					@Override
					ExtensionConfig create(String name) {
						return new StandardExtensionConfig( name, buildDetails )
					}
				}
		)
	}

	ExtensiblePolymorphicDomainObjectContainer<ExtensionConfig> getExtensionsContainer() {
		return extensionsContainer
	}

	HibernateOrmExtensionConfig hibernateOrm() {
		extensionsContainer.maybeCreate(HibernateOrmExtensionConfig.CONTAINER_NAME, HibernateOrmExtensionConfig.class)
	}

	@SuppressWarnings("unused")
	void hibernateOrm(Closure<HibernateOrmExtensionConfig> closure) {
		final HibernateOrmExtensionConfig ormExtension = hibernateOrm()
		ConfigureUtil.configure( closure, ormExtension )
	}

	@SuppressWarnings("unused")
	void hibernateOrm(Action<HibernateOrmExtensionConfig> action) {
		final HibernateOrmExtensionConfig ormExtension = hibernateOrm()
		action.execute( ormExtension )
	}


	@SuppressWarnings("unused")
	ExtensionConfig quarkusExtension(String shortName) {
		quarkusExtension( shortName, shortName )
	}

	ExtensionConfig quarkusExtension(String dslName, String shortName) {
		final ModuleVersionIdentifier runtimeArtifactId = new StandardModuleVersionIdentifier(
				Helper.QUARKUS_GROUP,
				Helper.QUARKUS + "-" + shortName,
				buildDetails.quarkusVersion
		)

		def extension = new StandardExtensionConfig(dslName, buildDetails, runtimeArtifactId.groupArtifactVersion())
		extensionsContainer.add(extension)
		return extension
	}

	void extension(String name, Closure closure) {
		def extension = extensionsContainer.maybeCreate( name )
		ConfigureUtil.configure( closure, extension )
	}

	void extension(String name, Action action) {
		def extension = extensionsContainer.maybeCreate( name )
		action.execute( extension )
	}

	def methodMissing(String name, etc) {
		// Groovy magic... called when a name in the `extensions {}` block is not recognized
		// we use that to trigger generation of a standard extension for configuration
		if ( etc == null ) {
			return extensionsContainer.maybeCreate( name )
		}
		else {
			def args = etc as Object[]
			if ( args.length == 1 ) {
				if ( args[0] instanceof Closure ) {
					return extensionsContainer.create( name, (Closure) args[0] )
				}
				else if ( args[0] instanceof Action ) {
					extensionsContainer.create( name, (Action) args[0] )
				}
			}
		}
		throw new GradleException( "Unable to interpret extension block: " + name )
	}
}
