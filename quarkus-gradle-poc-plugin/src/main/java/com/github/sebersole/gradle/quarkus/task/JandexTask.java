package com.github.sebersole.gradle.quarkus.task;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import org.jboss.jandex.Index;

import com.github.sebersole.gradle.quarkus.indexing.IndexManager;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * Jandex task for managing Indexes for each dependency
 */
public abstract class JandexTask extends DefaultTask {
	public static final String REGISTRATION_NAME = "quarkusJandex";

	public static JandexTask applyTo(Project project, Services services) {
		final JandexTask jandexTask = project.getTasks().create(
				REGISTRATION_NAME,
				JandexTask.class,
				services
		);
		jandexTask.setGroup( QUARKUS );
		jandexTask.setDescription( "Jandex Index management for Quarkus augmentation" );

//		services.getProjectService().visitAllProjects(
//				projectInfo -> {
//					final SourceSet mainSourceSet = projectInfo.getMainSourceSet();
//					final String compileJavaTaskName = mainSourceSet.getCompileJavaTaskName();
//
//					final Project visitedProject = project.project( projectInfo.getPath() );
//					final Task javacTask = visitedProject.getTasks().getByName( compileJavaTaskName );
//
//					jandexTask.dependsOn( javacTask );
//				}
//		);

		return jandexTask;
	}

	private final Services services;

	private Set<String> externalDependencies;

	@Inject
	public JandexTask(Services services) {
		this.services = services;
	}

	@OutputDirectory
	public Directory getOutputDirectory() {
		return services.getIndexingService().getJandexDirectory();
	}

	@Input
	public Set<String> getIndexedExternalArtifacts() {
		// NOTE : this should be called when determining the task-execution-graph
		//		which happens after project configuration/evaluation.  At this point,
		//		all dependencies should have been registered
		//
		// NOTE2 : for the external dependencies, we only care about the artifact id (group-artifact-id), not the file contents
		if ( externalDependencies == null ) {
			externalDependencies = services.getIndexingService()
					.getIndexedExternalArtifactBases()
					.stream()
					.map( File::getAbsolutePath )
					.collect( Collectors.toSet() );
		}

		return externalDependencies;
	}

	@Incremental
	@InputFiles
	public SourceSetOutput getIndexedProjectOutput() {
		// todo : need to figure out the best way to apply this to multiple projects.
		//		- one option is to generate a task per project-to-be-indexed
		return services.getProjectService().getMainProject().getMainSourceSet().getOutput();
	}

	@TaskAction
	public void manageIndexes(InputChanges inputChanges) {
		getLogger().trace( "Starting {} task", REGISTRATION_NAME );

		final HashSet<String> existingIndexFiles = new HashSet<>();
		getOutputDirectory().getAsFileTree().forEach( file -> existingIndexFiles.add( file.getAbsolutePath() ) );

		manageExternalArtifactIndexes( existingIndexFiles );
		manageProjectIndexes( existingIndexFiles, inputChanges );

		existingIndexFiles.forEach(
				noLongerNeededIndexFileName -> {
					final File file = getProject().file( noLongerNeededIndexFileName );
					file.delete();
				}
		);
	}

	private void manageExternalArtifactIndexes(Set<String> existingIndexFiles) {
		services.getIndexingService().forEachExternalArtifactIndexer(
				indexManager -> {
					// notes:
					//		`artifact` is the jar
					//		`indexManager#getIndexFile` is the index file
					final boolean previouslyIndexed = existingIndexFiles.remove( indexManager.getIndexFile().getAbsolutePath() );
					if ( previouslyIndexed ) {
						reloadIndex( indexManager );
					}
					else {
						generateIndex( indexManager );
					}
				}
		);
	}

	private void generateIndex(IndexManager indexManager) {
		final Index index = indexManager.generateIndex();
		services.getIndexingService().getCompositeIndex().expand( index );
	}

	private void reloadIndex(IndexManager indexManager) {
		final Index index = indexManager.readIndex();
		services.getIndexingService().getCompositeIndex().expand( index );
	}

	private void manageProjectIndexes(Set<String> existingIndexFiles, InputChanges inputChanges) {
		final IndexManager indexManager = services.getIndexingService().findIndexManagerByBase(
				services.getProjectService().getMainProject().getProjectDirectory().getAsFile()
		);

		assert indexManager != null;

		existingIndexFiles.remove( indexManager.getIndexFile().getAbsolutePath() );

		final Iterable<FileChange> fileChanges = inputChanges.getFileChanges( getIndexedProjectOutput() );
		final boolean hadChanges = fileChanges.iterator().hasNext();
		if ( hadChanges ) {
			generateIndex( indexManager );
		}
		else {
			reloadIndex( indexManager );
		}
	}
}
