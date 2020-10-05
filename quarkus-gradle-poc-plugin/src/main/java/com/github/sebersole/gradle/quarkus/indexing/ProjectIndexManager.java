package com.github.sebersole.gradle.quarkus.indexing;

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.SourceSet;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

import com.github.sebersole.gradle.quarkus.Logging;

/**
 * IndexManager for a local project
 */
public class ProjectIndexManager implements IndexManager {
	private final SourceSet mainSourceSet;
	private final File indexFile;

	private Index resolvedIndex;
	private boolean resolved;

	public ProjectIndexManager(SourceSet mainSourceSet, File indexFile) {
		this.mainSourceSet = mainSourceSet;
		this.indexFile = indexFile;
	}

	public SourceSet getSourceSet() {
		return mainSourceSet;
	}

	@Override
	public File getIndexFile() {
		return indexFile;
	}

	@Override
	public boolean isResolved() {
		return resolved;
	}

	@Override
	public Index getIndex() {
		if ( ! resolved ) {
			throw new GradleException( "Jandex indexes are not yet resolved" );
		}
		return resolvedIndex;
	}

	@Override
	public Index generateIndex() {
		assert !resolved;

		final Indexer indexer = new Indexer();
		mainSourceSet.getOutput().getClassesDirs().forEach(
				file -> JandexHelper.applyDirectory( file, indexer )
		);
		final Index index = indexer.complete();

		JandexHelper.writeIndexToFile( indexFile, index );

		return registerResolved( index );
	}

	private Index registerResolved(Index resolvedIndex) {
		this.resolvedIndex = resolvedIndex;
		this.resolved = true;

		return resolvedIndex;
	}

	@Override
	public Index readIndex() {
		if ( ! indexFile.exists() ) {
			// this condition can happen with TestKit (ftw!)
			Logging.LOGGER.debug( "Generating index in `#readIndex` because the index file does not exist : {}", indexFile.getAbsolutePath() );
			return generateIndex();
		}

		return registerResolved( JandexHelper.readJandexIndex( indexFile ) );
	}
}
