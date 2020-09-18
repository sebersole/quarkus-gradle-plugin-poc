package com.github.sebersole.gradle.quarkus;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.jupiter.api.Test;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class SimpleTest {

	@Test
	public void testBasicProjectLoading() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" );

		gradleRunner.build();
	}

	@Test
	public void testShowExtensions() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "showQuarkusExtensions" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusExtensions" );
		assertThat( taskResult, notNullValue() );
		MatcherAssert.assertThat( taskResult.getOutcome(), CoreMatchers.is( TaskOutcome.SUCCESS ) );

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
		MatcherAssert.assertThat( taskResult.getOutcome(), CoreMatchers.is( TaskOutcome.SUCCESS ) );

		validateExtensionListOutput( buildResult.getOutput() );
	}

	@Test
	public void testShowDependencies() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean", "showQuarkusDependencies", "--stacktrace" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusDependencies" );
		assertThat( taskResult, notNullValue() );
		MatcherAssert.assertThat( taskResult.getOutcome(), CoreMatchers.is( TaskOutcome.SUCCESS ) );

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
		MatcherAssert.assertThat( taskResult.getOutcome(), CoreMatchers.is( TaskOutcome.SUCCESS ) );

		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-hibernate-orm" ) );

		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "hibernate-core" ) );
		MatcherAssert.assertThat( buildResult.getOutput(), containsString( "caffeine-2.8.5.jar" ) );
	}

	@Test
	public void testJandexTask() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean", "quarkusJandex", "--stacktrace" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":quarkusJandex" );
		assertThat( taskResult, notNullValue() );
		MatcherAssert.assertThat( taskResult.getOutcome(), CoreMatchers.is( TaskOutcome.SUCCESS ) );
	}

	@Test
	public void testShowPersistenceUnitsTask() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean", "showPersistenceUnits", "--stacktrace" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showPersistenceUnits" );
		assertThat( taskResult, notNullValue() );
		assertThat( taskResult.getOutcome(), CoreMatchers.is( TaskOutcome.SUCCESS ) );

		assertThat( buildResult.getOutput(), containsString( "> Persistence Unit : abc" ) );
		assertThat( buildResult.getOutput(), containsString( "> Persistence Unit : xyz" ) );
	}

}
