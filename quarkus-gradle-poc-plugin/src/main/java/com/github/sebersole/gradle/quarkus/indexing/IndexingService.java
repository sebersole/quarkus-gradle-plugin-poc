package com.github.sebersole.gradle.quarkus.indexing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.gradle.api.file.Directory;

import org.jboss.jandex.Index;

import com.github.sebersole.gradle.quarkus.service.ProjectInfo;
import com.github.sebersole.gradle.quarkus.dependency.ExternalDependency;
import com.github.sebersole.gradle.quarkus.dependency.ProjectDependency;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.service.Service;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * A service for handling Jandex indexes
 */
public class IndexingService implements Service<IndexingService> {
	private final Directory jandexDirectory;
	private final Services services;

	private final Map<File,ProjectIndexManager> projectIndexManagers = new HashMap<>();
	private final Map<File, ExternalArtifactIndexManager> indexManagers = new HashMap<>();

	private final MutableCompositeIndex compositeIndex = new MutableCompositeIndex();

	public IndexingService(Services services) {
		assert services != null : "Services is null";

		this.jandexDirectory = services.getBuildDetails().getQuarkusWorkingDirectory().dir( JandexHelper.JANDEX );
		this.services = services;
	}

	@Override
	public Class<IndexingService> getRole() {
		return IndexingService.class;
	}

	public Directory getJandexDirectory() {
		return jandexDirectory;
	}

	public MutableCompositeIndex getCompositeIndex() {
		return compositeIndex;
	}


	public Set<File> getIndexedExternalArtifactBases() {
		return indexManagers.keySet();
	}

	public Set<File> getIndexedProjectBases() {
		return projectIndexManagers.keySet();
	}

	public void forEachProjectIndexer(Consumer<ProjectIndexManager> consumer) {
		projectIndexManagers.forEach(
				(file, projectIndexManager) -> consumer.accept( projectIndexManager )
		);
	}

	public void forEachProjectIndexer(BiConsumer<File,ProjectIndexManager> consumer) {
		projectIndexManagers.forEach(
				(file, projectIndexManager) -> consumer.accept( file, projectIndexManager )
		);
	}

	public void forEachExternalArtifactIndexer(Consumer<ExternalArtifactIndexManager > consumer) {
		indexManagers.forEach(
				(file, projectIndexManager) -> consumer.accept( projectIndexManager )
		);
	}

	public void forEachExternalArtifactIndexer(BiConsumer<File, ExternalArtifactIndexManager > consumer) {
		indexManagers.forEach(
				(file, projectIndexManager) -> consumer.accept( file, projectIndexManager )
		);
	}

	public IndexManager findIndexManagerByBase(File base) {
		return first(
				() -> projectIndexManagers.get( base ),
				() -> indexManagers.get( base )
		);
	}

	public IndexManager findIndexManagerByIndexFile(File indexFile) {
		return first(
				() -> first( projectIndexManagers.values(), manager -> manager.getIndexFile().equals( indexFile ) ),
				() -> first( indexManagers.values(), manager -> manager.getIndexFile().equals( indexFile ) )
		);
	}

	private <T> T first(Supplier<T>... suppliers) {
		for ( int i = 0; i < suppliers.length; i++ ) {
			final T supplied = suppliers[ i ].get();
			if ( supplied != null ) {
				return supplied;
			}
		}

		return null;
	}

	private static <T> T first(Iterable<T> values, Predicate<T> matcher) {
		for ( T value : values ) {
			if ( matcher.test( value ) ) {
				return value;
			}
		}
		return null;
	}


	/**
	 * Used when resolving a dependency to inject handling of the Jandex Index for that dependency
	 */
	public IndexAccess registerArtifactToIndex(ResolvedDependency resolvedDependency) {
		if ( resolvedDependency instanceof ProjectDependency ) {
			return registerProject( (ProjectDependency) resolvedDependency );
		}

		return registerExternalArtifact( (ExternalDependency) resolvedDependency );
	}

	private IndexAccess registerProject(ProjectDependency dependency) {
		final ProjectIndexManager existing = projectIndexManagers.get( dependency.getDependencyBase() );
		if ( existing != null ) {
			return new IndexAccess( existing );
		}

		final ProjectInfo projectInfo = services.getProjectService().findProjectInfoByPath( dependency.getProjectPath() );

		final ProjectIndexManager indexManager = new ProjectIndexManager(
				projectInfo.getMainSourceSet(),
				determineIndexFile( dependency, jandexDirectory )
		);

		projectIndexManagers.put( dependency.getDependencyBase(), indexManager );

		return new IndexAccess( indexManager );
	}

	private IndexAccess registerExternalArtifact(ExternalDependency dependency) {
		final ExternalArtifactIndexManager existing = indexManagers.get( dependency.getDependencyBase() );
		if ( existing != null ) {
			return new IndexAccess( existing );
		}

		final ExternalArtifactIndexManager indexManager = new ExternalArtifactIndexManager(
				dependency.getDependencyBase(),
				determineIndexFile( dependency, jandexDirectory )
		);

		indexManagers.put( dependency.getDependencyBase(), indexManager );

		return new IndexAccess( indexManager );
	}

	private static File determineIndexFile(ResolvedDependency dependency, Directory jandexDirectory) {
		final String indexFileName = JandexHelper.indexFileName( dependency );
		return jandexDirectory.file( indexFileName ).getAsFile();
	}

	public void loadIndexes() {
		projectIndexManagers.forEach(
				(file, indexManager) -> {
					final Index index = indexManager.readIndex();
					compositeIndex.expand( index );
				}
		);
		indexManagers.forEach(
				(file, indexManager) -> {
					final Index index = indexManager.readIndex();
					compositeIndex.expand( index );
				}
		);
	}
}
