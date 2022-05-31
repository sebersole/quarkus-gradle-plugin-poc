package com.github.sebersole.gradle.quarkus.indexing;

import java.io.File;

import org.jboss.jandex.Index;

/**
 * Responsible for managing a single Jandex Index.
 */
public interface IndexManager {
	File getIndexFile();
	Index getIndex();

	boolean isResolved();

	// these are the only 2 "resolve" forms
	Index generateIndex();
	Index readIndex();
}
