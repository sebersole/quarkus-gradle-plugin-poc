package com.github.sebersole.gradle.quarkus;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionAdapter;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.tasks.TaskState;
import org.gradle.util.GradleVersion;

import com.github.sebersole.gradle.quarkus.dsl.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.service.Services;
import com.github.sebersole.gradle.quarkus.task.AugmentationTask;
import com.github.sebersole.gradle.quarkus.task.GenerateFatJarTask;
import com.github.sebersole.gradle.quarkus.task.GenerateJarTask;
import com.github.sebersole.gradle.quarkus.task.JandexTask;
import com.github.sebersole.gradle.quarkus.task.ShowQuarkusDependenciesTask;
import com.github.sebersole.gradle.quarkus.task.ShowQuarkusExtensionsTask;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;


public class QuarkusPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		verifyGradleVersion();

		// assume the project must be a Java project for now
		project.getPlugins().apply( JavaLibraryPlugin.class );

		final Services services = new Services( project );

		final QuarkusSpec quarkusSpec = project.getExtensions().create(
				QUARKUS,
				QuarkusSpec.class,
				services
		);


		final JandexTask jandexTask = JandexTask.applyTo( project, services );
		jandexTask.setGroup( QUARKUS );
		jandexTask.setDescription( "Perform Quarkus-related Jandex indexing" );

		final AugmentationTask augmentationTask = AugmentationTask.applyTo( project, services );
		augmentationTask.dependsOn( jandexTask );

		final GenerateJarTask jarTask = GenerateJarTask.applyTo( project, services );
		jarTask.dependsOn( augmentationTask );

		final GenerateFatJarTask fatJarTask = GenerateFatJarTask.applyTo( project, services );
		fatJarTask.dependsOn( augmentationTask );

		ShowQuarkusDependenciesTask.applyTo( project, services );
		ShowQuarkusExtensionsTask.applyTo( project, services );

		project.afterEvaluate(
				p -> {
					assert p == project;

					// fully resolve extensions, handling any implicit extensions.  this triggers a number
					// of other actions including:
					// 		* resolving all runtime/deployment dependencies and registering the dependency with the
					//			DependencyService
					//		* recognize "implicit" or "transitive" extensions and register them (which may in turn
					//			trigger these same actions recursively on that implicit extension)
					//		* others per specific type of extension.  e.g. the Hibernate ORM extension will
					//			resolve and register persistence units
					services.getExtensionService().resolve( quarkusSpec.getExtensionSpecs() );
				}
		);

		project.getGradle().getTaskGraph().addTaskExecutionListener(
				new TaskExecutionAdapter() {
					@Override
					public void afterExecute(Task task, TaskState state) {
						if ( task.getName().equals( JandexTask.REGISTRATION_NAME ) ) {
							if ( ! state.getDidWork() ) {
								// the Jandex task was not run.  Force the IndexingService to
								// load all indexes
								project.getLogger().lifecycle( "Re-loading Jandex indexes" );
								services.getIndexingService().loadIndexes();
							}
						}
						super.afterExecute( task, state );
					}
				}
		);
	}

	private void verifyGradleVersion() {
		if ( GradleVersion.current().compareTo( GradleVersion.version( "5.0" ) ) < 0 ) {
			throw new GradleException(
					"Quarkus plugin requires Gradle 5.0 or later. Current version is: " + GradleVersion.current()
			);
		}
	}
}
