package com.github.sebersole.gradle.quarkus;

/**
 * Support for Gradle builds.  Located via {@link java.util.ServiceLoader} contract
 */
public interface IndexingSupport {
	/**
	 * Return {@code null} to indicate no indexing is needed on behalf of this extension
	 */
	IndexConsumer createIndexConsumer(BuildDetails buildDetails);
}
