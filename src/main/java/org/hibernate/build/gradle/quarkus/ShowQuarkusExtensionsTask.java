package org.hibernate.build.gradle.quarkus;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import static org.hibernate.build.gradle.quarkus.Helper.REPORT_BANNER_LINE;
import static org.hibernate.build.gradle.quarkus.Helper.REPORT_INDENTATION;
import static org.hibernate.build.gradle.quarkus.Helper.REPORT_INDENTATION_MARKER;

/**
 * @author Steve Ebersole
 */
public class ShowQuarkusExtensionsTask extends DefaultTask {
	private final QuarkusDsl buildConfig;

	@Inject
	public ShowQuarkusExtensionsTask(QuarkusDsl buildConfig) {
		this.buildConfig = buildConfig;
	}

	@TaskAction
	public void show() {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Quarkus Extensions Applied" );
		getLogger().lifecycle( REPORT_BANNER_LINE );

		buildConfig.getModules().forEach(
				extensionConfig -> getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " " + extensionConfig.getName() )
		);
	}
}
