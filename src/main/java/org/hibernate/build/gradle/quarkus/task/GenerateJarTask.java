package org.hibernate.build.gradle.quarkus.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import org.hibernate.build.gradle.quarkus.QuarkusDsl;

import static org.hibernate.build.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class GenerateJarTask extends DefaultTask {
	public static final String TASK_NAME = "generateQuarkusJar";

	public static GenerateJarTask task(QuarkusDsl dsl) {
		final GenerateJarTask task = dsl.getProject()
				.getTasks()
				.create( TASK_NAME, GenerateJarTask.class, dsl );

		task.setGroup( QUARKUS );
		task.setDescription( "Generates a Quarkus executable JAR" );

		return task;
	}

	@Inject
	public GenerateJarTask(QuarkusDsl quarkusDsl) {
	}

	@TaskAction
	public void generateJar() {
		getLogger().lifecycle( "Creating Quarkus executable JAR" );

		// ...
	}
}
