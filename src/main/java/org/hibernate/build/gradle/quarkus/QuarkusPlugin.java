package org.hibernate.build.gradle.quarkus;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

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
				project
		);

		// todo : what tasks are needed?
		//		- what needs to be done?

		final ShowQuarkusDependenciesTask showConfigTask = project
				.getTasks()
				.create( "showQuarkusDependencies", ShowQuarkusDependenciesTask.class, dsl );
		showConfigTask.setGroup( QUARKUS );
		showConfigTask.setDescription( "Outputs all Quarkus extension dependencies" );

		final ShowQuarkusExtensionsTask showExtensionsTask = project
				.getTasks()
				.create( "showQuarkusExtensions", ShowQuarkusExtensionsTask.class, dsl );
		showExtensionsTask.setGroup( QUARKUS );
		showExtensionsTask.setDescription( "Outputs all Quarkus extensions applied to the build" );
	}
}
