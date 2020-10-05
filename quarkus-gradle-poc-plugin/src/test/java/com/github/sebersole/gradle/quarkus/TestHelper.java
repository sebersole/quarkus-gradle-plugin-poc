/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.testkit.runner.GradleRunner;

/**
 * @author Steve Ebersole
 */
public class TestHelper {
	// our output is the base for all of the test projects, so we must be
	// able to locate that..  this is our `${buildDir}/resources/test` directory
	// within that directory we will have access to the `databases` dir as well
	// as the various test project root dirs

	public static File testProjectsBaseDirectory() {
		final URL baseUrl = TestHelper.class.getResource( "/project-directory.marker" );
		return new File( baseUrl.getFile() ).getParentFile();
	}

	public static File projectDirectory(String projectPath) {
		return new File( testProjectsBaseDirectory(), projectPath );
	}

	public static File jandexOutputDir(GradleRunner gradleRunner) {
		return jandexOutputDir( gradleRunner.getProjectDir() );
	}

	public static File jandexOutputDir(File projectDirectory) {
		final File gradleBuildDir = new File( projectDirectory, "build" );
		final File quarkusOutputDir = new File( gradleBuildDir, "quarkus" );
		return new File( quarkusOutputDir, "jandex" );
	}

	public static GradleRunner createGradleRunner(String projectPath, String... tasks) {
		final File projectsBaseDirectory = testProjectsBaseDirectory();
		final File projectDirectory = new File( projectsBaseDirectory, projectPath );

		final File tempDir = new File(
				projectsBaseDirectory.getParentFile().getParentFile(),
				"tmp"
		);
		final File testKitDir = new File( tempDir, "test-kit" );

		final GradleRunner gradleRunner = GradleRunner.create()
				.withPluginClasspath()
				.withProjectDir( projectDirectory )
				.withTestKitDir( testKitDir )
				.forwardOutput()
				.withDebug( true );

		if ( tasks == null ) {
			return gradleRunner.withArguments( "--stacktrace" );
		}
		else {
			final List<String> arguments = new ArrayList<>( Arrays.asList( tasks ) );
			arguments.add( "--stacktrace" );
			return gradleRunner.withArguments( arguments );
		}
	}
}
