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
	private final QuarkusDsl quarkusDsl;

	@Inject
	public ShowQuarkusDependenciesTask(QuarkusDsl quarkusDsl) {
		this.quarkusDsl = quarkusDsl;
	}

	@TaskAction
	public void show() {
		quarkusDsl.getModules().forEach(
				extensionDsl -> showConfiguration( extensionDsl.getDependencyConfiguration() )
		);

		showConfiguration( quarkusDsl.getRuntimeConfiguration() );
	}

	private void showConfiguration(Configuration dependencyConfiguration) {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Quarkus `" +  dependencyConfiguration.getName() + "` Configuration");
		getLogger().lifecycle( REPORT_BANNER_LINE );

		getLogger().lifecycle( REPORT_INDENTATION + REPORT_INDENTATION_MARKER + " Dependencies" );

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
