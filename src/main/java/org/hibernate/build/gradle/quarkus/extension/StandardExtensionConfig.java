package org.hibernate.build.gradle.quarkus.extension;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard "extension config" implementation
 *
 * @author Steve Ebersole
 */
public class StandardExtensionConfig implements ExtensionConfig {
	private final String name;
	private final Map<Object,Object> properties = new HashMap<>();

	public StandardExtensionConfig(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public Map<?, ?> getProperties() {
		return properties;
	}

	public void property(Object key, Object value) {
		applyProperty( key, value );
	}

	protected <K,V> V applyProperty(K key, V value) {
		//noinspection unchecked
		return (V) properties.put( key, value );
	}
}
