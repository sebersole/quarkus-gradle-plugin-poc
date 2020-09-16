package com.github.sebersole.gradle.quarkus;

/**
 * Support for Gradle builds.  Located via {@link java.util.ServiceLoader} contract
 */
public interface GradleSupport {
	/**
	 * Return {@code null} to indicate no indexing is needed on behalf of this extension
	 */
	Indexer createIndexer(BuildDetails quarkusDsl);
}
