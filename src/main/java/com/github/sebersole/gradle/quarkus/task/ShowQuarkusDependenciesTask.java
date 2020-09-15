package com.github.sebersole.gradle.quarkus.task;

import java.io.File;
import java.util.Set;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;
import com.github.sebersole.gradle.quarkus.extension.Extension;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_BANNER_LINE;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_INDENTATION;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_INDENTATION_MARKER;

/**
 * @author Steve Ebersole
 */
public class ShowQuarkusDependenciesTask extends DefaultTask {
	public static final String TASK_NAME = "showQuarkusDependencies";

	public static ShowQuarkusDependenciesTask task(QuarkusDsl dsl) {
		final ShowQuarkusDependenciesTask task = dsl.getProject()
				.getTasks()
				.create( TASK_NAME, ShowQuarkusDependenciesTask.class, dsl );

		task.setGroup( QUARKUS );
		task.setDescription( "Shows dependency information per Quarkus extension.  Can also call `showQuarkusDependencies_<extension>` to limit the info to just the named extension" );

		return task;
	}


	private final QuarkusDsl quarkusDsl;

	@Inject
	public ShowQuarkusDependenciesTask(QuarkusDsl quarkusDsl) {
		this.quarkusDsl = quarkusDsl;
		setGroup( QUARKUS );
		setDescription( "Outputs all Quarkus extension dependencies" );

		quarkusDsl.getProject().getTasks().addRule(
				"Pattern: showQuarkusDependencies_<extension>",
				taskName -> {
					if ( taskName.startsWith( "showQuarkusDependencies" )
							&& taskName.contains( "_" )
							&& ! taskName.endsWith( "showQuarkusDependencies" ) ) {
						// parse the extension name
						final int delimiterPosition = taskName.indexOf( '_' );
						assert delimiterPosition > 1;
						final String extensionName = taskName.substring( delimiterPosition + 1 );
						final Extension extensionInfo = quarkusDsl.getModules().getByName( extensionName );

						final Task task = quarkusDsl.getProject().task( taskName );
						task.doLast(
								(task1) -> showConfiguration( extensionInfo.getDependencies() )
						);
					}
				}
		);
	}

	@TaskAction
	public void show() {
		quarkusDsl.getModules().forEach(
				extension -> showConfiguration( extension.getDependencies() )
		);

		showConfiguration( quarkusDsl.getRuntimeConfiguration() );
	}

	private void showConfiguration(Configuration dependencyConfiguration) {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Quarkus `" +  dependencyConfiguration.getName() + "` Configuration");
		getLogger().lifecycle( "{} {} - {}", REPORT_INDENTATION, REPORT_INDENTATION, dependencyConfiguration.getDescription() );
		getLogger().lifecycle( REPORT_BANNER_LINE );

		getLogger().lifecycle( REPORT_INDENTATION + "> Dependencies" );

		for ( Dependency dependency : dependencyConfiguration.getAllDependencies() ) {
			final String coordinate = Helper.groupArtifactVersion( dependency.getGroup(), dependency.getName(), dependency.getVersion() );
			getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " " + coordinate );
		}


		final Set<File> files = dependencyConfiguration.resolve();

		getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " Files" );

		for ( File file : files ) {
			getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " " + file.getName() );
		}
	}
}
