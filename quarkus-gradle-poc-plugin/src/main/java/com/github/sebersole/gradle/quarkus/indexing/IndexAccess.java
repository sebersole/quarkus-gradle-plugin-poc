package com.github.sebersole.gradle.quarkus.indexing;

import org.gradle.api.GradleException;

import org.jboss.jandex.IndexView;

/**
 * Indirection reference to a Jandex index for a single dependency.
 *
 * Code can use this as an immutable reference and leverage it as
 * a provider for the index.
 */
public class IndexAccess {
	private final IndexManager indexManager;

	public IndexAccess(IndexManager indexManager) {
		this.indexManager = indexManager;
	}

	public IndexView getIndex() {
		if ( ! indexManager.isResolved() ) {
			throw new GradleException( "Jandex indexes are not yet resolved" );
		}
		return indexManager.getIndex();
	}
}
