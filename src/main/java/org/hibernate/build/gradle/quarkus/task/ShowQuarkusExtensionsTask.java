package org.hibernate.build.gradle.quarkus.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import org.hibernate.build.gradle.quarkus.QuarkusDsl;
import org.hibernate.build.gradle.quarkus.extension.ExtensionIdentifier;

import static org.hibernate.build.gradle.quarkus.Helper.QUARKUS;
import static org.hibernate.build.gradle.quarkus.Helper.REPORT_BANNER_LINE;
import static org.hibernate.build.gradle.quarkus.Helper.REPORT_INDENTATION;

/**
 * @author Steve Ebersole
 */
public class ShowQuarkusExtensionsTask extends DefaultTask {
	private final QuarkusDsl buildConfig;

	@Inject
	public ShowQuarkusExtensionsTask(QuarkusDsl buildConfig) {
		this.buildConfig = buildConfig;
		setGroup( QUARKUS );
		setDescription( "Outputs all Quarkus extensions applied to the build" );
	}

	public static ShowQuarkusExtensionsTask task(QuarkusDsl dsl) {
		final ShowQuarkusExtensionsTask task = dsl.getProject()
				.getTasks()
				.create( "showQuarkusExtensions", ShowQuarkusExtensionsTask.class, dsl );

		task.setGroup( QUARKUS );
		task.setDescription( "Shows applied Quarkus extensions" );

		final Task listExtensionsTask = dsl.getProject()
				.getTasks()
				.create( "listExtensions", Task.class );
		listExtensionsTask.setGroup( QUARKUS );
		listExtensionsTask.setDescription( "Synonym for `showQuarkusExtensions`" );
		listExtensionsTask.dependsOn( task );

		return task;
	}

	@TaskAction
	public void show() {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Quarkus Extensions" );
		getLogger().lifecycle( REPORT_BANNER_LINE );

		buildConfig.getModules().forEach(
				extensionConfig -> {
					final ExtensionIdentifier extensionIdentifier = extensionConfig.getIdentifier();
					getLogger().lifecycle( "{} > {}", REPORT_INDENTATION, extensionIdentifier.getQuarkusArtifactId() );
				}
		);
	}
}
