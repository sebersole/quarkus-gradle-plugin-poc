package com.github.sebersole.gradle.quarkus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.dependency.MutableCompositeIndex;

/**
 * @author Steve Ebersole
 */
public class BuildState {
	private final Map<String, Extension> extensionsByGav = new HashMap<>();
	private final Map<String, ResolvedDependency> resolvedDependencyByGav = new HashMap<>();
	private final MutableCompositeIndex compositeIndex = new MutableCompositeIndex();

	public Extension findExtensionByGav(String gav) {
		return extensionsByGav.get( gav );
	}

	public void registerExtensionByGav(String gav, Extension extension) {
		final Extension existing = extensionsByGav.put( gav, extension );
		assert existing == null;
	}

	public ResolvedDependency locateResolvedDependency(String gav, Supplier<ResolvedDependency> creator) {
		final ResolvedDependency existing = findResolvedDependency( gav );
		if ( existing != null ) {
			return existing;
		}

		final ResolvedDependency created = creator.get();
		registerResolvedDependency( gav, created );

		return created;
	}

	public ResolvedDependency findResolvedDependency(String gav) {
		return resolvedDependencyByGav.get( gav );
	}

	public void registerResolvedDependency(String gav, ResolvedDependency resolvedDependency) {
		final ResolvedDependency previous = resolvedDependencyByGav.put( gav, resolvedDependency );
		assert previous == null;
		compositeIndex.expand( resolvedDependency.getJandexIndex() );
	}

	public void visitResolvedDependencies(Consumer<ResolvedDependency> consumer) {
		resolvedDependencyByGav.forEach(
				(gav, resolvedDependency) -> consumer.accept( resolvedDependency )
		);
	}
}
