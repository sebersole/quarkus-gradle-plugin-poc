package com.github.sebersole.gradle.quarkus.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * Generates a Quarkus jar
 */
public class GenerateJarTask extends DefaultTask {
	public static final String TASK_NAME = "generateQuarkusJar";

	public static GenerateJarTask applyTo(Project project, Services services) {
		final GenerateJarTask task = project
				.getTasks()
				.create( TASK_NAME, GenerateJarTask.class, services );

		task.setGroup( QUARKUS );
		task.setDescription( "Generates a Quarkus executable JAR" );

		return task;
	}

	@Inject
	public GenerateJarTask(Services services) {
	}

	@TaskAction
	public void generateJar() {
		getLogger().trace( "Creating Quarkus executable JAR" );

		// ...
	}
}
