package com.github.sebersole.gradle.quarkus.task;

import java.io.File;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import org.jboss.jandex.Index;

import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.indexing.IndexAccess;
import com.github.sebersole.gradle.quarkus.indexing.JandexHelper;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * Responsible for managing the Jandex Index for a single dependency
 */
public class JandexTask extends DefaultTask {
	public static final String TASK_NAME = "quarkusJandex";

	public static IndexAccess apply(String gav, ResolvedArtifact resolvedArtifact, QuarkusDslImpl quarkusDsl) {
		final Project project = quarkusDsl.getProject();

		final String uniqueName = JandexHelper.makeUniqueName( resolvedArtifact );
		final String jandexTaskName = TASK_NAME + "_" + uniqueName;
		final String indexFileName = uniqueName + ".idx";

		final RegularFile indexFile = quarkusDsl.getWorkingDir().dir( "jandex" ).file( indexFileName );

		final JandexTask jandexTask = project.getTasks().create(
				jandexTaskName,
				JandexTask.class,
				gav,
				resolvedArtifact.getFile(),
				indexFile,
				quarkusDsl
		);
		jandexTask.setGroup( QUARKUS );
		jandexTask.setDescription( "Manages Jandex index related with `" + gav + "`" );

		final Task groupingTask = project.getTasks().getByName( TASK_NAME );
		groupingTask.dependsOn( jandexTask );

		return new IndexAccess( gav, jandexTask::accessJandexIndex, quarkusDsl );
	}

	private final String gav;
	private final File dependencyArtifactBase;
	private final RegularFile indexFile;

	private final QuarkusDslImpl quarkusDsl;

	private Index index;
	private boolean resolved;


	@Inject
	public JandexTask(String gav, File dependencyArtifactBase, RegularFile indexFile, QuarkusDslImpl quarkusDsl) {
		this.gav = gav;
		this.dependencyArtifactBase = dependencyArtifactBase;
		this.indexFile = indexFile;

		this.quarkusDsl = quarkusDsl;

		setGroup( QUARKUS );
		setDescription( "Manages Jandex index related with `" + gav + "`" );
	}

	@Input
	public String getGav() {
		return gav;
	}

	@InputFile
	public File getDependencyArtifactBase() {
		return dependencyArtifactBase;
	}

	@OutputFile
	public RegularFile getIndexFile() {
		return indexFile;
	}

	@TaskAction
	public void resolveIndex() {
		this.index = JandexHelper.resolveJandexIndex( gav, dependencyArtifactBase, indexFile, quarkusDsl );
		this.resolved = true;
	}

	public Index accessJandexIndex() {
		if ( resolved ) {
			return index;
		}

		Logging.LOGGER.debug( "Resolving Jandex index for dependency `{}`", gav );

		final boolean beingExecuted = getProject().getGradle().getTaskGraph().hasTask( this );
		if ( beingExecuted ) {
			resolveIndex();
			return index;
		}

		// otherwise, just load the index file we've created previously...
		assert indexFile.getAsFile().exists();
		this.index = JandexHelper.readJandexIndex( indexFile.getAsFile() );
		this.resolved = true;

		return index;
	}
}
