package com.github.sebersole.gradle.quarkus;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.util.GradleVersion;

import com.github.sebersole.gradle.quarkus.task.AugmentationTask;
import com.github.sebersole.gradle.quarkus.task.GenerateFatJarTask;
import com.github.sebersole.gradle.quarkus.task.GenerateJarTask;
import com.github.sebersole.gradle.quarkus.task.JandexTask;
import com.github.sebersole.gradle.quarkus.task.ShowQuarkusDependenciesTask;
import com.github.sebersole.gradle.quarkus.task.ShowQuarkusExtensionsTask;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class QuarkusPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		verifyGradleVersion();

		final QuarkusDslImpl dsl = project.getExtensions().create(
				QUARKUS,
				QuarkusDslImpl.class,
				project
		);

		final JandexTask[] jandexTasks = JandexTask.apply( dsl );

		final AugmentationTask augmentationTask = AugmentationTask.task( dsl );
		//noinspection RedundantCast
		augmentationTask.dependsOn( (Object[]) jandexTasks );

		final GenerateJarTask jarTask = GenerateJarTask.task( dsl );
		jarTask.dependsOn( augmentationTask );

		final GenerateFatJarTask fatJarTask = GenerateFatJarTask.task( dsl );
		fatJarTask.dependsOn( augmentationTask );

		final ShowQuarkusDependenciesTask showConfigTask = ShowQuarkusDependenciesTask.task( dsl );
		final ShowQuarkusExtensionsTask showExtensionsTask = ShowQuarkusExtensionsTask.task( dsl );
	}

	private void verifyGradleVersion() {
		if ( GradleVersion.current().compareTo( GradleVersion.version( "5.0" ) ) < 0 ) {
			throw new GradleException(
					"Quarkus plugin requires Gradle 5.0 or later. Current version is: " + GradleVersion.current()
			);
		}
	}
}
