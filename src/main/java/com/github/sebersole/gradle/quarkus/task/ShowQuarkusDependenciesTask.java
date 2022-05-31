package com.github.sebersole.gradle.quarkus.task;

import java.io.File;
import java.util.Set;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_BANNER_LINE;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_INDENTATION;

/**
 * Task for showing Quarkus dependencies, by extension
 */
public class ShowQuarkusDependenciesTask extends DefaultTask {
	public static final String TASK_NAME = "showQuarkusDependencies";

	public static ShowQuarkusDependenciesTask applyTo(Project project, Services services) {
		final ShowQuarkusDependenciesTask task = project
				.getTasks()
				.create( TASK_NAME, ShowQuarkusDependenciesTask.class, services );

		task.setGroup( QUARKUS );
		task.setDescription( "Shows dependency information per Quarkus extension.  Can also call `showQuarkusDependencies_<extension>` to limit the info to just the named extension" );

		project.getTasks().addRule(
				"Pattern: showQuarkusDependencies_<extension>",
				taskName -> {
					if ( taskName.startsWith( "showQuarkusDependencies" )
							&& taskName.contains( "_" )
							&& ! taskName.endsWith( "showQuarkusDependencies" ) ) {
						// parse the extension name
						final int delimiterPosition = taskName.indexOf( '_' );
						assert delimiterPosition > 1;
						final String extensionName = taskName.substring( delimiterPosition + 1 );
						final Extension extensionInfo = services.getExtensionService().getByName( extensionName );

						final Task ruleTask = project.getTasks().create( taskName );
						ruleTask.doLast(
								(task1) -> task.showExtension( extensionInfo )
						);
					}
				}
		);

		return task;
	}


	private final Services services;

	@Inject
	public ShowQuarkusDependenciesTask(Services services) {
		this.services = services;
		setGroup( QUARKUS );
		setDescription( "Outputs all Quarkus extension dependencies" );
	}

	@TaskAction
	public void show() {
		services.getExtensionService().visitExtensions( this::showExtension );
	}

	private void showExtension(Extension extension) {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Dependency information for the {} Extension", extension.getName() );
		getLogger().lifecycle( REPORT_BANNER_LINE );

		showConfiguration( extension.getRuntimeDependencies() );
		showConfiguration( extension.getDeploymentDependencies() );
	}

	private void showConfiguration(Configuration dependencies) {
		getLogger().lifecycle(
				"{} - Dependencies ({})",
				REPORT_INDENTATION,
				dependencies.getName()
		);

		getLogger().lifecycle(
				"{}{} > Artifacts",
				REPORT_INDENTATION,
				REPORT_INDENTATION
		);

		for ( Dependency dependency : dependencies.getAllDependencies() ) {
			final String coordinate = Helper.groupArtifactVersion( dependency.getGroup(), dependency.getName(), dependency.getVersion() );
			getLogger().lifecycle(
					"{}{}{}- {}",
					REPORT_INDENTATION,
					REPORT_INDENTATION,
					REPORT_INDENTATION,
					coordinate
			);
		}

		getLogger().lifecycle(
				"{}{} > Files",
				REPORT_INDENTATION,
				REPORT_INDENTATION
		);

		final Set<File> files = dependencies.resolve();

		for ( File file : files ) {
			getLogger().lifecycle(
					"{}{}{}- {}",
					REPORT_INDENTATION,
					REPORT_INDENTATION,
					REPORT_INDENTATION,
					file.getName()
			);
		}
	}
}
