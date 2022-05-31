package com.github.sebersole.gradle.quarkus.service;

/**
 * General contract for a service that can be used with {@link Services}
 */
public interface Service<T> {
	/**
	 * The exposed role of the service.
	 */
	Class<T> getRole();
}
