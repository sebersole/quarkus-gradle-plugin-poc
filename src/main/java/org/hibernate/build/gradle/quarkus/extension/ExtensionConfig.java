package org.hibernate.build.gradle.quarkus.extension;

import java.util.Map;

/**
 * Basic contract for an "extension config" object
 *
 * @author Steve Ebersole
 */
public interface ExtensionConfig {
	String getName();

	Map<?,?> getProperties();

	void property(Object key, Object value);
}
