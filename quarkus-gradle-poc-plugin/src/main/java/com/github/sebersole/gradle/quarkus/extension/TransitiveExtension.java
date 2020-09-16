package com.github.sebersole.gradle.quarkus.extension;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;

/**
 * @author Steve Ebersole
 */
public class TransitiveExtension extends AbstractExtension {
	public TransitiveExtension(String dslContainerName, String gav, QuarkusDslImpl quarkusDsl) {
		super(
				dslContainerName,
				(extension, quarkusDsl1) -> new Artifact( gav ),
				(extension, quarkusDsl1) -> {
					final int groupArtifactSeparatorPosition = gav.indexOf( ':' );
					assert groupArtifactSeparatorPosition > 0;

					final int artifactVersionSeparatorPosition = gav.indexOf( ':', groupArtifactSeparatorPosition + 1 );
					assert artifactVersionSeparatorPosition > 1;

					final String groupId = gav.substring( 0, groupArtifactSeparatorPosition );
					final String moduleName = gav.substring( groupArtifactSeparatorPosition + 1, artifactVersionSeparatorPosition ) + "-deployment";
					final String rest = gav.substring( artifactVersionSeparatorPosition + 1 );

					return new Artifact( Helper.groupArtifactVersion( groupId, moduleName, rest ) );
				},
				quarkusDsl
		);
	}
}
