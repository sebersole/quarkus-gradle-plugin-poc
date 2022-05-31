package com.github.sebersole.gradle.quarkus.jpa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.ClassInfo;

/**
 * A "resolved" persistence-unit descriptor
 */
public class PersistenceUnit {
	private final String unitName;

	private Set<ClassInfo> classesToInclude;

	public PersistenceUnit(String unitName) {
		this.unitName = unitName;
	}

	public String getUnitName() {
		return unitName;
	}

	public Set<ClassInfo> getClassesToInclude() {
		return classesToInclude;
	}

	/**
	 * package visibility
	 */
	void applyClassToInclude(ClassInfo classInfo) {
		if ( classesToInclude == null ) {
			classesToInclude = new HashSet<>();
		}

		classesToInclude.add( classInfo );
	}
}
