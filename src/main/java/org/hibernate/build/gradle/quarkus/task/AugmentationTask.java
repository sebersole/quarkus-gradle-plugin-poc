package org.hibernate.build.gradle.quarkus.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import org.hibernate.build.gradle.quarkus.QuarkusDsl;

import static org.hibernate.build.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class AugmentationTask extends DefaultTask {
	public static final String TASK_NAME = "quarkusAugmentation";

	public static AugmentationTask task(QuarkusDsl dsl) {
		final AugmentationTask augmentationTask = dsl.getProject()
				.getTasks()
				.create( TASK_NAME, AugmentationTask.class, dsl );
		augmentationTask.setGroup( QUARKUS );
		augmentationTask.setDescription( "Performs Quarkus augmentation" );

		return augmentationTask;
	}

	// todo : inputs?
	// todo : outputs?

	@Inject
	public AugmentationTask(QuarkusDsl quarkusDsl) {
	}

	@TaskAction
	public void augment() {
		getLogger().lifecycle( "Starting Quarkus augmentation" );
	}
}
