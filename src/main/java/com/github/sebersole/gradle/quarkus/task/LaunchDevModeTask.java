package com.github.sebersole.gradle.quarkus.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.QuarkusDsl;

/**
 * @author Steve Ebersole
 */
public class LaunchDevModeTask extends DefaultTask {
	public LaunchDevModeTask(QuarkusDsl quarkusDsl) {
		dependsOn( AugmentationTask.TASK_NAME );
	}

	@TaskAction
	public void launchDevMode() {
		getLogger().lifecycle( "Launching Quarkus dev-mode" );

		throw new UnsupportedOperationException( "Not yet implemented" );
	}
}
