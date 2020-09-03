package org.hibernate.build.gradle.quarkus;

import java.io.File;
import java.util.Set;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;

import static org.hibernate.build.gradle.quarkus.Helper.REPORT_BANNER_LINE;
import static org.hibernate.build.gradle.quarkus.Helper.REPORT_INDENTATION;
import static org.hibernate.build.gradle.quarkus.Helper.REPORT_INDENTATION_MARKER;

/**
 * @author Steve Ebersole
 */
public class ShowQuarkusDependenciesTask extends DefaultTask {
	private final Configuration configuration;

	@Inject
	public ShowQuarkusDependenciesTask(Configuration configuration) {
		this.configuration = configuration;
		dependsOn( configuration );
	}

	@TaskAction
	public void show() {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Quarkus `" +  configuration.getName() + "` Configuration");
		getLogger().lifecycle( REPORT_BANNER_LINE );

		getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " Dependencies" );

		for ( Dependency dependency : configuration.getAllDependencies() ) {
			final String coordinate = Helper.coordinate( dependency.getGroup(), dependency.getName(), dependency.getVersion() );
			getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " " + coordinate );
		}


		getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " Files" );

		final Set<File> files = configuration.resolve();
		for ( File file : files ) {
			getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " " + file.getName() );
		}
	}
}
