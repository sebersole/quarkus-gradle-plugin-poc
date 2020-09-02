package org.hibernate.build.gradle.quarkus;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Steve Ebersole
 */
public class QuarkusPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final QuarkusBuildConfig dsl = project.getExtensions().create(
				"quarkus",
				QuarkusBuildConfig.class,
				this,
				project
		);

		// todo : what tasks are needed?
		//		- what needs to be done?
	}


}
