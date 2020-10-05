package com.github.sebersole.gradle.quarkus.indexing;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.jar.JarFile;

import org.gradle.api.GradleException;

import org.jboss.jandex.Index;

import com.github.sebersole.gradle.quarkus.Logging;

/**
 * Standard IndexManager implementation
 */
public class ExternalArtifactIndexManager implements IndexManager, Serializable {
	private final File indexingBase;
	private final File indexFile;

	private Index resolvedIndex;
	private boolean resolved;

	public ExternalArtifactIndexManager(File indexingBase, File indexFile) {
		assert indexingBase.isFile();
		assert indexingBase.exists();

		this.indexingBase = indexingBase;
		this.indexFile = indexFile;
	}

	@Override
	public File getIndexFile() {
		return indexFile;
	}

	@Override
	public Index getIndex() {
		if ( ! resolved ) {
			throw new GradleException( "Jandex indexes not yet resolved" );
		}
		return resolvedIndex;
	}

	@Override
	public boolean isResolved() {
		return resolved;
	}

	@Override
	public Index generateIndex() {
		assert ! resolved;
		resolvedIndex = internalResolve();

		return resolvedIndex;
	}

	private Index internalResolve() {
		try {
			final JarFile jarFile = new JarFile( indexingBase );
			return internalResolve( jarFile );
		}
		catch (IOException e) {
			Logging.LOGGER.debug( "Exception trying to handle dependency as a JAR : `{}`", indexFile.getAbsolutePath() );
			return null;
		}
	}

	private Index internalResolve(JarFile jarFile) {
		final Index index = JandexHelper.resolveIndexFromArchive( jarFile, indexingBase );
		JandexHelper.writeIndexToFile( indexFile, index );

		injectResolvedIndex( index );

		return index;
	}

	private void injectResolvedIndex(Index index) {
		if ( resolved ) {
			Logging.LOGGER.debug( "Overriding Jandex Index with injected one for `{}`", indexFile.getAbsolutePath() );
		}

		this.resolvedIndex = index;
		this.resolved = true;


	}

	@Override
	public Index readIndex() {
		final Index index = JandexHelper.readJandexIndex( indexFile );
		injectResolvedIndex( index );
		return index;
	}
}
