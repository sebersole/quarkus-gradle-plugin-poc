package com.github.sebersole.gradle.quarkus.task;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class JandexTask extends DefaultTask {
	public static JandexTask createTask(QuarkusDslImpl dsl) {
		final JandexTask  jandexTask = dsl.getProject()
				.getTasks()
				.create( TASK_NAME, JandexTask.class, dsl );
		jandexTask.setGroup( QUARKUS );
		jandexTask.setDescription( "Generates a Jandex index in preparation for augmentation" );

		return jandexTask;
	}

	public static final String TASK_NAME = "quarkusJandex";

	private File outputDirectory;

	// todo : do these indexes need to be kept per-project?  Or is one big index better?

	// todo : is there a way to know which sub-project sourceSets should be indexed?  Without the user having to configure them.
	//		-e.g., automatically add all subproject main sourcesets
	private Set<SourceDirectorySet> sourcesToIndex;

	@Inject
	public JandexTask(QuarkusDslImpl quarkusDsl) {
		final Project project = quarkusDsl.getProject();
		assert project != null : "Project was null";

		this.sourcesToIndex = new HashSet<>();
		applyProject( project, this.sourcesToIndex::add, quarkusDsl );

		final File quarkusOutputDirectory = new File( project.getBuildDir(), "quarkus" );
		this.outputDirectory = new File( quarkusOutputDirectory, "jandex" );

		doFirst( JandexTask::makeOutputDirectory );
	}

	private static void makeOutputDirectory(Task task) {
		assert task instanceof JandexTask;

		//noinspection ResultOfMethodCallIgnored
		( (JandexTask) task ).outputDirectory.mkdirs();
	}

	private static void applyProject(
			Project project,
			Consumer<SourceDirectorySet> sourceDirectorySetConsumer,
			QuarkusDsl quarkusDsl) {
		final Convention convention = project.getConvention();

		final JavaPluginConvention javaPluginConvention = convention.findPlugin( JavaPluginConvention.class );
		if ( javaPluginConvention == null ) {
			Logging.LOGGER.debug( "Skipping project `{}`; it defined no JavaPluginConvention", project.getPath() );
		}
		else {
			final SourceSetContainer sourceSets = javaPluginConvention.getSourceSets();

			final SourceSet mainSourceSet = sourceSets.findByName( "main" );
			if ( mainSourceSet == null ) {
				Logging.LOGGER.debug( "Skipping project `{}`; it defined no `main` SourceSet", project.getPath() );
			}
			else {
				Logging.LOGGER.debug( "Adding main SourceSet ({})", project.getPath() );
				sourceDirectorySetConsumer.accept( mainSourceSet.getAllJava() );
			}
		}

		project.getSubprojects().forEach(
				subproject -> applyProject( subproject, sourceDirectorySetConsumer, quarkusDsl )
		);
	}

	@OutputDirectory
	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	@InputFiles
	public Set<SourceDirectorySet> getSourcesToIndex() {
		return sourcesToIndex;
	}

	public void setSourcesToIndex(Set<SourceDirectorySet> sourcesToIndex) {
		this.sourcesToIndex = sourcesToIndex;
	}

	@TaskAction
	public void generateIndex() {
		getLogger().lifecycle( "########################################################" );
		getLogger().lifecycle( "Generating Quarkus Jandex index" );
		getLogger().lifecycle( "########################################################" );

		sourcesToIndex.forEach(
				sources -> {
					getLogger().lifecycle( "  > SourceSet ({})", sources.getDisplayName() );
					sources.getSrcDirs().forEach(
							srcDir -> getLogger().lifecycle( "    > {}", srcDir.getPath() )
					);
				}
		);

		getLogger().lifecycle( "########################################################" );
	}
}
