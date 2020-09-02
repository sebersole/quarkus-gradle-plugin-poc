package org.hibernate.build.gradle.quarkus;

import org.gradle.api.NamedDomainObjectFactory;

import org.hibernate.build.gradle.quarkus.extension.ExtensionConfig;
import org.hibernate.build.gradle.quarkus.extension.HibernateOrmExtensionConfig;
import org.hibernate.build.gradle.quarkus.extension.StandardExtensionConfig;

/**
 * Factory for ExtensionConfig references
 *
 * @author Steve Ebersole
 */
public class ExtensionConfigCreator implements NamedDomainObjectFactory<ExtensionConfig> {
	private final QuarkusBuildConfig quarkusBuildConfig;

	public ExtensionConfigCreator(QuarkusBuildConfig quarkusBuildConfig) {
		this.quarkusBuildConfig = quarkusBuildConfig;
	}

	@Override
	public ExtensionConfig create(String name) {
		final String moduleName = "quarkus-" + asModuleName( name );

		if ( "quarkus-hibernate-orm".equals( moduleName ) ) {
			return new HibernateOrmExtensionConfig( moduleName, quarkusBuildConfig );
		}

		return new StandardExtensionConfig( moduleName );
	}

	private String asModuleName(String name) {
		final StringBuilder buffer = new StringBuilder();
		for ( int i = 0; i < name.length(); i++ ) {
			final char originalChar = name.charAt( i );
			if ( Character.isUpperCase( originalChar ) ) {
				buffer.append( '-' ).append( Character.toLowerCase( originalChar ) );
			}
			else {
				buffer.append( originalChar );
			}
		}
		return buffer.toString();
	}
}
