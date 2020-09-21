package com.github.sebersole.gradle.quarkus.indexing;

import java.io.File;

import org.gradle.api.file.RegularFile;

import org.jboss.jandex.Index;

import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;

/**
 * Resolves a Jandex Index for a specific dependency.
 *
 * This might mean either reading from a dependency's `META-INF/jandex.idx`
 * file (if one) or building one from the dependency's artifact represented
 * by base which should be a jar or directory.
 *
 * The generated 
 */
@FunctionalInterface
public interface IndexResolver {
	/**
	 * Resolve the Index
	 */
	Index resolveIndex(String gav, File base, RegularFile outputFile, QuarkusDslImpl quarkusDsl);
}
