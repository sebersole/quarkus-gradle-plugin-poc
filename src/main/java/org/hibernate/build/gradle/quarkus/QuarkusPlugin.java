package org.hibernate.build.gradle.quarkus;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import org.hibernate.build.gradle.quarkus.extension.ExtensionModuleCreationListener;
import org.hibernate.build.gradle.quarkus.task.AugmentationTask;
import org.hibernate.build.gradle.quarkus.task.GenerateFatJarTask;
import org.hibernate.build.gradle.quarkus.task.GenerateJarTask;
import org.hibernate.build.gradle.quarkus.task.JandexTask;
import org.hibernate.build.gradle.quarkus.task.ShowQuarkusDependenciesTask;
import org.hibernate.build.gradle.quarkus.task.ShowQuarkusExtensionsTask;

import static org.hibernate.build.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class QuarkusPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final QuarkusDsl dsl = project.getExtensions().create(
				QUARKUS,
				QuarkusDsl.class,
				project,
				(ExtensionModuleCreationListener) extensionDsl -> {}
		);

		final JandexTask jandexTask = JandexTask.createTask( dsl );

		final AugmentationTask augmentationTask = AugmentationTask.task( dsl );
		augmentationTask.dependsOn( jandexTask );

		final GenerateJarTask jarTask = GenerateJarTask.task( dsl );
		jarTask.dependsOn( augmentationTask );

		final GenerateFatJarTask fatJarTask = GenerateFatJarTask.task( dsl );
		fatJarTask.dependsOn( augmentationTask );


		final ShowQuarkusDependenciesTask showConfigTask = ShowQuarkusDependenciesTask.task( dsl );
		final ShowQuarkusExtensionsTask showExtensionsTask = ShowQuarkusExtensionsTask.task( dsl );

		project.subprojects(
				(subproject) -> {
					// todo : what do we need to do here for sub-projects?
					// 		- https://github.com/quarkusio/quarkus/issues/5722

					project.getLogger().lifecycle( "" );
				}
		);
	}
}
