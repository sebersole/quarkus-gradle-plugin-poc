package com.github.sebersole.gradle.quarkus.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * Generates a Quarkus fat-jar
 */
public class GenerateFatJarTask extends DefaultTask {
	public static GenerateFatJarTask applyTo(Project project, Services services) {
		final GenerateFatJarTask task = project
				.getTasks()
				.create( GenerateFatJarTask.TASK_NAME, GenerateFatJarTask.class, services );

		task.setGroup( QUARKUS );
		task.setDescription( "Generates a Quarkus fat JAR" );

		return task;
	}

	public static final String TASK_NAME = "generateQuarkusFatJar";

	@Inject
	public GenerateFatJarTask(Services services) {
	}

	@TaskAction
	public void generateJar() {
		getLogger().trace( "Creating Quarkus fat executable JAR" );

		// ...
	}
}
