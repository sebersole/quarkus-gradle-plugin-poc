package com.github.sebersole.gradle.quarkus;

import org.gradle.api.file.SourceDirectorySet;

/**
 * Support for performing indexing on behalf of the extension
 */
public interface Indexer {
	void index(SourceDirectorySet sourceDirectorySet);
}
