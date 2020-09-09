package org.hibernate.build.gradle.quarkus.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import org.hibernate.build.gradle.quarkus.QuarkusDsl;

import static org.hibernate.build.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class GenerateFatJarTask extends DefaultTask {
	public static GenerateFatJarTask task(QuarkusDsl dsl) {
		final GenerateFatJarTask task = dsl.getProject()
				.getTasks()
				.create( GenerateFatJarTask.TASK_NAME, GenerateFatJarTask.class, dsl );

		task.setGroup( QUARKUS );
		task.setDescription( "Generates a Quarkus fat JAR" );

		return task;
	}

	public static final String TASK_NAME = "generateQuarkusFatJar";

	@Inject
	public GenerateFatJarTask(QuarkusDsl quarkusDsl) {
	}

	@TaskAction
	public void generateJar() {
		getLogger().lifecycle( "Creating Quarkus fat executable JAR" );

		// ...
	}
}
