package org.hibernate.build.gradle.quarkus;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import org.hibernate.build.gradle.quarkus.extension.ExtensionModuleCreationListener;

import static org.hibernate.build.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class QuarkusPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final QuarkusDsl dsl = project.getExtensions().create(
				QUARKUS,
				QuarkusDsl.class,
				project,
				(ExtensionModuleCreationListener) extensionDsl -> {}
		);

		// todo : what tasks are needed?
		//		- what needs to be done?

		// here are some simple ones...

		final ShowQuarkusDependenciesTask showConfigTask = project
				.getTasks()
				.create( "showQuarkusDependencies", ShowQuarkusDependenciesTask.class, dsl );

		final ShowQuarkusExtensionsTask showExtensionsTask = project
				.getTasks()
				.create( "showQuarkusExtensions", ShowQuarkusExtensionsTask.class, dsl );

	}
}
