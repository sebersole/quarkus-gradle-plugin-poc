package com.github.sebersole.gradle.quarkus;

import org.jboss.jandex.IndexView;

/**
 * Support for consuming a Jandex index and doing something with it - the exact nature
 * of the "something" depends on the underlying extension.
 */
@FunctionalInterface
public interface IndexConsumer {
	void consume(IndexView indexView);
}
