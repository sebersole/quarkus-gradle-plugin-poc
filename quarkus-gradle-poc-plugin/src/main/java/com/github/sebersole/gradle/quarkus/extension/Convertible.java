package com.github.sebersole.gradle.quarkus.extension;

import com.github.sebersole.gradle.quarkus.dsl.ExtensionSpec;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Capability to convert an ExtensionConfig into an Extension
 */
public interface Convertible<T extends Extension> extends ExtensionSpec {
	T convert(Services services);
}
