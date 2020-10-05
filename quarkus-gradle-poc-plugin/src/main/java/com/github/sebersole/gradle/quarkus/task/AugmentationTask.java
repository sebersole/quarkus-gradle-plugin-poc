package com.github.sebersole.gradle.quarkus.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;

/**
 * Catch-all for Quarkus building
 */
public class AugmentationTask extends DefaultTask {
	public static final String REGISTRATION_NAME = "quarkusAugmentation";

	public static AugmentationTask applyTo(Project project, Services services) {
		final AugmentationTask augmentationTask = project
				.getTasks()
				.create( REGISTRATION_NAME, AugmentationTask.class, services );
		augmentationTask.setGroup( QUARKUS );
		augmentationTask.setDescription( "Performs Quarkus augmentation" );
		return augmentationTask;
	}

	private final Services services;

	// todo : inputs?
	// todo : outputs?

	@Inject
	public AugmentationTask(Services services) {
		this.services = services;
	}

	@TaskAction
	public void augment() {
		getLogger().trace( "Starting {} task", REGISTRATION_NAME );
	}
}
