package com.github.sebersole.gradle.quarkus;

import java.io.File;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import static com.github.sebersole.gradle.quarkus.TestHelper.jandexOutputDir;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;

/**
 * @author Steve Ebersole
 */
public class SimpleTest {
//	@BeforeAll
//	public static void cleanOutputs() {
//		final File projectDirectory = TestHelper.projectDirectory( "simple" );
//		final File jandexOutputDir = jandexOutputDir( projectDirectory );
//		jandexOutputDir.delete();
//
//		final File buildDir = new File( projectDirectory, "build" );
//		final File gradleWorkDir = new File( buildDir, ".gradle" );
//		gradleWorkDir.delete();
//	}

	@Test
	public void testBasicProjectLoading() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" );

		gradleRunner.build();
	}

	@Test
	public void testCleaning() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean" );

		final BuildResult results = gradleRunner.build();

		final BuildTask cleanTaskResult = results.task( ":clean" );
		assertThat( cleanTaskResult.getOutcome(), CoreMatchers.anyOf( is( TaskOutcome.SUCCESS ), is( TaskOutcome.UP_TO_DATE ) ) );

		final File jandexDir = jandexOutputDir( gradleRunner );
		assertThat( jandexDir, not( anExistingDirectory() ) );
	}

	@Test
	public void testShowExtensions() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "showQuarkusExtensions" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusExtensions" );
		assertThat( taskResult, notNullValue() );
		MatcherAssert.assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		validateExtensionListOutput( buildResult.getOutput() );
	}

	private void validateExtensionListOutput(String output) {
		assertThat( output, containsString( "io.quarkus:quarkus-hibernate-orm" ) );
		assertThat( output, containsString( "io.quarkus:quarkus-jdbc-derby" ) );
		assertThat( output, containsString( "io.quarkus:quarkus-hibernate-validator" ) );

		assertThat( output, not( containsString( "hibernate-core" ) ) );
	}

	@Test
	public void testListExtensions() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "listExtensions" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusExtensions" );
		assertThat( taskResult, notNullValue() );
		MatcherAssert.assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		validateExtensionListOutput( buildResult.getOutput() );
	}

	@Test
	public void testShowDependencies() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean", "showQuarkusDependencies", "--stacktrace" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusDependencies" );
		assertThat( taskResult, notNullValue() );
		MatcherAssert.assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-hibernate-orm" ) );
		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-jdbc-derby" ) );
		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-hibernate-validator" ) );

		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "hibernate-core" ) );
		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "caffeine-2.8.5.jar" ) );
	}

	@Test
	public void testShowLimitedDependencies() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean", "showQuarkusDependencies_hibernateOrm", "--stacktrace" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusDependencies_hibernateOrm" );
		assertThat( taskResult, notNullValue() );
		MatcherAssert.assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-hibernate-orm" ) );

		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "hibernate-core" ) );
		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "caffeine-2.8.5.jar" ) );
	}

	@Test
	@Disabled( "https://discuss.gradle.org/t/testkit-and-up-to-date-checking/37684/2" )
	public void testJandexTask() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" );

		final File jandexDir = jandexOutputDir( gradleRunner );

		// Executing clean here does not seem to have any affect:
		//		1) hard clean the directory
		//		2) we need to verify this actually works in a "real" consumer project


		final BuildResult buildResult = gradleRunner.withArguments( "clean", "quarkusJandex", "--stacktrace" ).build();

		final BuildTask taskResult = buildResult.task( ":quarkusJandex" );
		assertThat( taskResult, notNullValue() );
		MatcherAssert.assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		// make sure the indexes were generated
		assertThat( jandexDir, anExistingDirectory() );

		final File[] files = jandexDir.listFiles();
		assertThat( files, not( emptyArray() ) );

		for ( int i = 0; i < files.length; i++ ) {
			assertThat( files[i], anExistingFile() );
			assertThat( files[i].getName(), endsWith( ".idx" ) );
		}
	}

	@Test
	public void testShowPersistenceUnitsTask() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean", "showPersistenceUnits", "--stacktrace" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showPersistenceUnits" );
		assertThat( taskResult, notNullValue() );
		assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		assertThat( buildResult.getOutput(), containsString( "> Persistence Unit : abc" ) );
		assertThat( buildResult.getOutput(), containsString( "> Persistence Unit : xyz" ) );
	}

}
