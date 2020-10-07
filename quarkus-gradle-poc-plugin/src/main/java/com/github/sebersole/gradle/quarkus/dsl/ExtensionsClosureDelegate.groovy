package com.github.sebersole.gradle.quarkus.dsl


import org.gradle.util.ConfigureUtil

import com.github.sebersole.gradle.quarkus.service.BuildDetails

/**
 * Used as the Closure delegate for Groovy access to the ExtensionSpecContainer
 *
 * Provides access to:
 * 		1) container of explicitly declared extensions
 * 		2) extension creation capabilities for "well known" extensions
 */
class ExtensionsClosureDelegate {
	private final BuildDetails buildDetails
	private final ExtensionSpecContainer extensionSpecContainer

	ExtensionsClosureDelegate(BuildDetails buildDetails, ExtensionSpecContainer extensionSpecContainer) {
		this.buildDetails = buildDetails
		this.extensionSpecContainer = extensionSpecContainer
	}

	HibernateOrmExtensionSpec hibernateOrm() {
		extensionSpecContainer.hibernateOrm()
	}

	@SuppressWarnings("unused")
	void hibernateOrm(Closure<HibernateOrmExtensionSpec> closure) {
		extensionSpecContainer.hibernateOrm( closure )
	}

	@SuppressWarnings("unused")
	ExtensionSpec quarkusExtension(String shortName) {
		extensionSpecContainer.quarkusExtension( shortName );
	}

	ExtensionSpec quarkusExtension(String dslName, String shortName) {
		extensionSpecContainer.quarkusExtension( dslName, shortName )
	}

	ExtensionSpec quarkusExtension(String dslName, String shortName, Closure closure) {
		def extension = extensionSpecContainer.quarkusExtension(dslName, shortName)
		closure.call( extension )
	}

//	void extension(String name, Closure<ExtensionSpec> closure) {
//		extensionSpecContainer.extension( name, closure )
//	}

	void extension(String name, Class specType, Closure closure) {
		extensionSpecContainer.extension( name, specType, closure )
	}

	def methodMissing(String name, etc) {
		// Groovy magic... called when a name in the `extensions {}` block is not recognized
		// we use that to trigger generation of a standard extension for configuration
		def args = etc as Object[]
		if ( args == null || args.length == 0 ) {
			return extensionSpecContainer.container.maybeCreate(name)
		}

		// `etc` might contain a few permutations...
		// 1) ExtensionConfig implementor class-name
		// 2) ExtensionConfig implementor class-name + closure
		// 3) closure

		// NOTE : does not handle Action because generally speaking Action would be passed from Kotlin scripts
		// 		which would not understand this Groovy concept...

		if ( args.length > 2 ) {
			throw new UnsupportedOperationException( "Unsure how to handle extension configuration with `" + args.length + "` arguments" )
		}

		if ( args.length == 2 ) {
			// we have case #2
			def extensionConfig = extensionSpecContainer.container.maybeCreate(name, (Class) args[0])
			ConfigureUtil.configure( (Closure) args[1], extensionConfig )
			return extensionConfig
		}

		// otherwise, we have #1 or #3
		if ( args[0] instanceof Closure ) {
			def extensionConfig = extensionSpecContainer.container.maybeCreate(name)
			ConfigureUtil.configure( (Closure) args[0], extensionConfig )
			return extensionConfig
		}
		else {
			return extensionSpecContainer.container.maybeCreate(name, (Class) args[0])
		}
	}
}
