package com.github.sebersole.gradle.quarkus.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_BANNER_LINE;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_INDENTATION;

/**
 * Task for showing all Quarkus extensions
 */
public class ShowQuarkusExtensionsTask extends DefaultTask {
	public static ShowQuarkusExtensionsTask applyTo(Project project, Services services) {
		final ShowQuarkusExtensionsTask showExtensionsTask = project
				.getTasks()
				.create( "showQuarkusExtensions", ShowQuarkusExtensionsTask.class, services );

		showExtensionsTask.setGroup( QUARKUS );
		showExtensionsTask.setDescription( "Shows applied Quarkus extensions" );

		final Task listExtensionsTask = project
				.getTasks()
				.create( "listExtensions", Task.class );
		listExtensionsTask.setGroup( QUARKUS );
		listExtensionsTask.setDescription( "Synonym for `showQuarkusExtensions`" );
		listExtensionsTask.dependsOn( showExtensionsTask );

		return showExtensionsTask;
	}

	private final Services services;

	@Inject
	public ShowQuarkusExtensionsTask(Services services) {
		this.services = services;

		setGroup( QUARKUS );
		setDescription( "Outputs all Quarkus extensions applied to the build" );
	}

	@TaskAction
	public void show() {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Quarkus Extensions" );
		getLogger().lifecycle( REPORT_BANNER_LINE );

		services.getExtensionService().visitExtensions(
				extension -> {
					final String artifactId = extension.getArtifact().groupArtifactVersion();
					getLogger().lifecycle( "{} > {}", REPORT_INDENTATION, artifactId );
				}
		);
	}
}
