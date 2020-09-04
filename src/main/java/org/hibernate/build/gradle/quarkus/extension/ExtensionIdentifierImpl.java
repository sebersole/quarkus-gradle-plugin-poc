package org.hibernate.build.gradle.quarkus.extension;

/**
 * @author Steve Ebersole
 */
public class ExtensionIdentifierImpl implements ExtensionIdentifier {
	private final String dslContainerName;
	private final String artifactId;
	private final String displayName;

	public ExtensionIdentifierImpl(String artifactId) {
		assert artifactId.startsWith( "quarkus-" );

		this.artifactId = artifactId;
		this.dslContainerName = artifactId.substring( "quarkus-".length() );
		this.displayName = extractCamelCaseName( dslContainerName );
	}

	private static String extractCamelCaseName(String extensionName) {
		final StringBuilder buff = new StringBuilder();

		final char[] chars = extensionName.toCharArray();
		for ( int i = 0; i < chars.length; i++ ) {
			if ( '-' == chars[ i ] ) {
				// skip the dash
				i++;
				buff.append( Character.toUpperCase( chars[ i ] ) );
			}
			else {
				buff.append( chars[ i ] );
			}
		}

		return buff.toString();
	}

	@Override
	public String getDslContainerName() {
		return dslContainerName;
	}

	@Override
	public String getQuarkusArtifactId() {
		return artifactId;
	}

	@Override
	public String getCamelCaseName() {
		return displayName;
	}
}
