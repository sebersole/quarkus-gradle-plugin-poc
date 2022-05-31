package com.github.sebersole.gradle.quarkus.extension;

import com.github.sebersole.gradle.quarkus.dsl.ExtensionConfig;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Capability to convert an ExtensionConfig into an Extension
 */
public interface Convertible<T extends Extension> extends ExtensionConfig {
	T convert(Services services);
}
