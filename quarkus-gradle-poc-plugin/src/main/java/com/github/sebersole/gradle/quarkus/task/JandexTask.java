package com.github.sebersole.gradle.quarkus.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import org.jboss.jandex.Index;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependencyFactory;

import static com.github.sebersole.gradle.quarkus.Helper.JANDEX_INDEX_FILE_PATH;
import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * Creates Jandex index for the project modules
 *
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

	private final Project project;
	private final QuarkusDslImpl quarkusDsl;

	// todo : [NOTE] At the moment we only index the projects' main source-set.  This matches
	//  	the existing behavior, but allowing indexing more than one source-set seems nicer
	private SourceSet sourceSetToIndex;

	@Inject
	public JandexTask(Project project, QuarkusDslImpl quarkusDsl) {
		assert project != null : "Project was null";
		this.project = project;
		this.quarkusDsl = quarkusDsl;

		// atm we only support indexing the main source-set
		this.sourceSetToIndex = resolveMainSourceSet( project );
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

	@InputFiles
	public FileCollection getSourcesToIndex() {
		return sourceSetToIndex.getAllJava();
	}

	public void setSourcesToIndex(SourceSet sourceSetToIndex) {
		this.sourceSetToIndex = sourceSetToIndex;
	}

	public void sourcesToIndex(SourceSet sourceSetToIndex) {
		this.sourceSetToIndex = sourceSetToIndex;
	}

	@TaskAction
	public void generateIndex() {
		Logging.LOGGER.trace( "Starting Jandex indexing for project `{}`", project.getPath() );

		final String projectGroupId = project.getGroup().toString();
		final String projectArtifactId = project.getName();
		final String projectVersion = project.getVersion().toString();

		final String projectGav = Helper.groupArtifactVersion(
				projectGroupId,
				projectArtifactId,
				projectVersion
		);

		quarkusDsl.getBuildState().locateResolvedDependency(
				projectGav,
				() -> {
					final SourceDirectorySet allJavaSources = sourceSetToIndex.getAllJava();

					final Directory classesDirectoryRef = allJavaSources.getClassesDirectory().getOrNull();

					if ( classesDirectoryRef == null ) {
						// nothing to index.  really this should never happen outside of testing
						// because of how TestKit works
						return new ResolvedDependency(
								projectGroupId,
								projectArtifactId,
								projectVersion,
								new File(
										new File(
												new File(
														project.getBuildDir(),
														"classes"
												),
												"java"
										),
										"main"
								),
								null
						);
					}

					final File classesDirectory = classesDirectoryRef.getAsFile();
					assert classesDirectory.isDirectory();

					final Index jandexIndex = ResolvedDependencyFactory.createJandexIndexFromDirectory( classesDirectory, project, quarkusDsl );

					final File resourcesOutputDirectory = sourceSetToIndex.getResources().getDestinationDirectory().get().getAsFile();
					final File jandexFile = new File( resourcesOutputDirectory, JANDEX_INDEX_FILE_PATH );
					Helper.ensureFileExists( jandexFile, quarkusDsl );

					ResolvedDependencyFactory.writeIndexToFile( jandexFile, jandexIndex, project, quarkusDsl );

					return new ResolvedDependency(
							projectGroupId,
							projectArtifactId,
							projectVersion,
							classesDirectory,
							jandexIndex
					);
				}
		);
	}
}
