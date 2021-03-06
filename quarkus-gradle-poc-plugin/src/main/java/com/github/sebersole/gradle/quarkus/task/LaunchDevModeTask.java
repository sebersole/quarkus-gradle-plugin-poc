package com.github.sebersole.gradle.quarkus.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.dsl.QuarkusConfig;

/**
 * Placeholder for "dev mode"
 */
public class LaunchDevModeTask extends DefaultTask {
	public LaunchDevModeTask(QuarkusConfig quarkusConfig) {
		dependsOn( AugmentationTask.REGISTRATION_NAME );
	}

	@TaskAction
	public void launchDevMode() {
		getLogger().trace( "Launching Quarkus dev-mode" );

		throw new UnsupportedOperationException( "Not yet implemented" );
	}
}
