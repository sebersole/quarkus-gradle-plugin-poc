package com.github.sebersole.gradle.quarkus.extension.orm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.ClassInfo;

import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.dependency.MutableCompositeIndex;

/**
 * @author Steve Ebersole
 */
public class PersistenceUnit {
	private final String unitName;
	private final MutableCompositeIndex compositeJandexIndex;

	private Set<ClassInfo> classesToInclude;

	public PersistenceUnit(String unitName, QuarkusDslImpl quarkusDsl) {
		this.unitName = unitName;
		this.compositeJandexIndex = quarkusDsl.getBuildState().getCompositeJandexIndex();
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

	public void applyClassesToInclude(Collection<ClassInfo> implementors) {
		if ( implementors == null || implementors.isEmpty() ) {
			return;
		}

		if ( classesToInclude == null ) {
			classesToInclude = new HashSet<>();
		}

		classesToInclude.addAll( implementors );
	}
}
