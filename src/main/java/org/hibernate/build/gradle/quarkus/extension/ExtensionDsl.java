package org.hibernate.build.gradle.quarkus.extension;

import java.io.Serializable;
import java.util.Map;

/**
 * Basic contract for an "extension config" object
 *
 * @author Steve Ebersole
 */
public interface ExtensionDsl extends Serializable {
	String getName();

	Map<?,?> getProperties();

	void property(Object key, Object value);
}
