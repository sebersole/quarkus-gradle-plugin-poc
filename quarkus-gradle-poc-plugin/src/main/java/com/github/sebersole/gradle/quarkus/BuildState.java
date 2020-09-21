package com.github.sebersole.gradle.quarkus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.jandex.IndexView;

import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.dependency.MutableCompositeIndex;

/**
 * @author Steve Ebersole
 */
public class BuildState {
	private final Map<String, Extension> extensionsByGav = new HashMap<>();
	private final Map<String, ResolvedDependency> resolvedDependencyByGav = new HashMap<>();

	private MutableCompositeIndex compositeIndex;

	public MutableCompositeIndex getResolvedCompositeIndex() {
		if ( compositeIndex == null ) {
			compositeIndex = new MutableCompositeIndex();
			visitResolvedDependencies( compositeIndex::expand );
		}

		return compositeIndex;
	}

	public void withCompositeJandexIndex(Consumer<IndexView> indexConsumer) {
		indexConsumer.accept( getResolvedCompositeIndex() );
	}

	public Extension locateExtensionByGav(String gav, Supplier<Extension> creator) {
		final Extension extensionByGav = findExtensionByGav( gav );
		if ( extensionByGav != null ) {
			return extensionByGav;
		}

		final Extension created = creator.get();
		registerExtensionByGav( gav, created );
		return created;
	}

	public Extension findExtensionByGav(String gav) {
		return extensionsByGav.get( gav );
	}

	public void registerExtensionByGav(String gav, Extension extension) {
		final Extension existing = extensionsByGav.put( gav, extension );
		assert existing == null : "Extension already registered under GAV : " + gav;
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
	}

	public void visitResolvedDependencies(Consumer<ResolvedDependency> consumer) {
		resolvedDependencyByGav.forEach(
				(gav, resolvedDependency) -> consumer.accept( resolvedDependency )
		);
	}
}
