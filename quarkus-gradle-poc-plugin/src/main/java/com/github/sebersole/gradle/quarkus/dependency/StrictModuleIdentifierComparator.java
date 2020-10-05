package com.github.sebersole.gradle.quarkus.dependency;

import java.util.Comparator;
import java.util.Objects;

/**
 * Strict comparator for ModuleIdentifier references.  Meaning even if the arguments
 * happen to be versioned we ignore that
 */
public class StrictModuleIdentifierComparator implements Comparator<ModuleIdentifier> {

	@Override
	public int compare(ModuleIdentifier o1, ModuleIdentifier o2) {
		final int groupComparison = Objects.compare( o1.getGroupName(), o2.getGroupName(), String::compareTo );
		if ( groupComparison != 0 ) {
			return groupComparison;
		}

		return Objects.compare( o1.getArtifactName(), o2.getArtifactName(), String::compareTo );
	}
}
