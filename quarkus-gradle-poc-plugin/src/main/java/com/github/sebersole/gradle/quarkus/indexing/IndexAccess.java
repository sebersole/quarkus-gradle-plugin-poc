package com.github.sebersole.gradle.quarkus.indexing;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import org.gradle.api.GradleException;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;

import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;

/**
 * Indirection reference to a Jandex index for a single dependency.
 *
 * Code can use this class as a immutable reference and leverage it as
 * a Provider for the index.  The Supplier it receives itself ultimately
 * comes from the corresponding indexing task.  The index itself is only
 * available once the indexing has been performed.
 */
public class IndexAccess implements Provider<IndexView> {
	private final String gav;
	private final Supplier<Index> indexSupplier;

	private final QuarkusDslImpl quarkusDsl;


	public IndexAccess(String gav, Supplier<Index> indexSupplier, QuarkusDslImpl quarkusDsl) {
		this.gav = gav;
		this.indexSupplier = indexSupplier;
		this.quarkusDsl = quarkusDsl;
	}

	public String getGav() {
		return gav;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Provider

	@Override
	public IndexView get() {
		final IndexView index = getOrNull();
		if ( index == null ) {
			throw new GradleException( "Could not access Jandex index" );
		}
		return index;
	}

	@Nullable
	@Override
	public IndexView getOrNull() {
		return indexSupplier.get();
	}

	@Override
	public IndexView getOrElse(IndexView alternative) {
		final IndexView index = getOrNull();
		return index != null ? index : alternative;
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super IndexView> transformer) {
		return quarkusDsl.getProject().provider( () -> transformer.transform( get() ) );
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super IndexView> transformer) {
		//noinspection unchecked
		return (Provider<S>) transformer.transform( get() );
	}

	@Override
	public boolean isPresent() {
		return indexSupplier.get() != null;
	}

	@Override
	public Provider<IndexView> orElse(IndexView alternative) {
		return quarkusDsl.getProject().provider( () -> getOrElse( alternative ) );
	}

	@Override
	public Provider<IndexView> orElse(Provider<? extends IndexView> alternativeProvider) {
		return quarkusDsl.getProject().provider(
				() -> {
					final Index index = indexSupplier.get();
					return index != null
							? index
							: alternativeProvider.getOrNull();
				}
		);
	}
}
