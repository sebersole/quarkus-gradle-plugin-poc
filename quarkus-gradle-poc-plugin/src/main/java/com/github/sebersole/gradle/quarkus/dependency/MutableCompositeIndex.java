package com.github.sebersole.gradle.quarkus.dependency;

import java.util.Collection;
import java.util.Collections;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

/**
 * @author Steve Ebersole
 */
public class MutableCompositeIndex implements IndexView {
	private IndexView delegate;

	public void expand(IndexView addition) {
		if ( addition == null ) {
			return;
		}

		if ( delegate == null ) {
			delegate = addition;
		}
		else {
			delegate = CompositeIndex.create( delegate, addition );
		}
	}

	public void expand(ResolvedDependency dependency) {
		expand( dependency.getJandexIndexAccess().get() );
	}

	@Override
	public Collection<ClassInfo> getKnownClasses() {
		if ( delegate == null ) {
			return Collections.emptyList();
		}

		return delegate.getKnownClasses();
	}

	@Override
	public ClassInfo getClassByName(DotName className) {
		if ( delegate == null ) {
			return null;
		}

		return delegate.getClassByName( className );
	}

	@Override
	public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
		if ( delegate == null ) {
			return Collections.emptyList();
		}

		return delegate.getKnownDirectSubclasses( className );
	}

	@Override
	public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
		if ( delegate == null ) {
			return Collections.emptyList();
		}

		return delegate.getAllKnownSubclasses( className );
	}

	@Override
	public Collection<ClassInfo> getKnownDirectImplementors(DotName className) {
		if ( delegate == null ) {
			return Collections.emptyList();
		}

		return delegate.getKnownDirectImplementors( className );
	}

	@Override
	public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
		if ( delegate == null ) {
			return Collections.emptyList();
		}

		return delegate.getAllKnownImplementors( interfaceName );
	}

	@Override
	public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
		if ( delegate == null ) {
			return Collections.emptyList();
		}

		return delegate.getAnnotations( annotationName );
	}

	@Override
	public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index) {
		if ( delegate == null ) {
			return Collections.emptyList();
		}

		return delegate.getAnnotationsWithRepeatable( annotationName, index );
	}
}
