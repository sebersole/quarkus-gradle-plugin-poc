package com.github.sebersole.gradle.quarkus.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.Indexer;
import com.github.sebersole.gradle.quarkus.IndexingSupport;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class JandexTask extends DefaultTask {

	public static JandexTask[] apply(QuarkusDslImpl dsl) {
		final List<JandexTask> tasks = new ArrayList<>();
		apply( dsl.getProject(), dsl, tasks::add );
		return tasks.toArray( new JandexTask[0] );
	}

	private static void apply(Project project, QuarkusDslImpl dsl, Consumer<JandexTask> taskConsumer) {
		if ( project.getPlugins().hasPlugin( JavaPlugin.class ) ) {
			final JandexTask jandexTask = project
					.getTasks()
					.create( TASK_NAME, JandexTask.class, project, dsl );
			jandexTask.setGroup( QUARKUS );
			jandexTask.setDescription( "Generates a Jandex index in preparation for augmentation" );

			taskConsumer.accept( jandexTask );
		}

		project.subprojects( subProject -> apply( subProject, dsl, taskConsumer ) );
	}

	public static final String TASK_NAME = "quarkusJandex";
	public static final String INDEX_FILE_PATH = "META-INF/jandex.idx";

	private final Project project;
	private final QuarkusDslImpl quarkusDsl;

	private final File outputDirectory;

	// todo : do these indexes need to be kept per-project?  Or is one big index better?

	// todo : is there a way to know which sub-project sourceSets should be indexed?  Without the user having to configure them.
	//		-e.g., automatically add all subproject main sourcesets
	private Set<SourceDirectorySet> sourcesToIndex;

	@Inject
	public JandexTask(Project project, QuarkusDslImpl quarkusDsl) {
		assert project != null : "Project was null";
		this.project = project;
		this.quarkusDsl = quarkusDsl;

		final SourceSet mainSourceSet = resolveMainSourceSet( project );
		// atm we only support indexing the main source-set
		this.sourcesToIndex = Collections.singleton( mainSourceSet.getAllJava() );

		final File resourcesOutputDir = new File( new File( project.getBuildDir(), "resources" ), "main" );
		this.outputDirectory = new File( resourcesOutputDir, INDEX_FILE_PATH );

		doFirst( JandexTask::makeOutputDirectory );
	}

	private static void makeOutputDirectory(Task task) {
		assert task instanceof JandexTask;

		//noinspection ResultOfMethodCallIgnored
		( (JandexTask) task ).outputDirectory.mkdirs();
	}

	private static SourceSet resolveMainSourceSet(Project project) {
		final Convention convention = project.getConvention();

		final JavaPluginConvention javaPluginConvention = convention.findPlugin( JavaPluginConvention.class );
		if ( javaPluginConvention == null ) {
			Logging.LOGGER.debug( "Skipping project `{}`; it defined no JavaPluginConvention", project.getPath() );
			return null;
		}

		final SourceSet mainSourceSet = javaPluginConvention.getSourceSets().findByName( "main" );
		if ( mainSourceSet == null ) {
			Logging.LOGGER.debug( "Skipping project `{}`; it defined no `main` SourceSet", project.getPath() );
			return null;
		}

		Logging.LOGGER.debug( "Adding main SourceSet ({})", project.getPath() );
		return mainSourceSet;
	}

	@OutputDirectory
	public File getOutputDirectory() {
		return outputDirectory;
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
		quarkusDsl.getQuarkusExtensions().forEach(
				extension -> sourcesToIndex.forEach( extension::index )
		);

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
