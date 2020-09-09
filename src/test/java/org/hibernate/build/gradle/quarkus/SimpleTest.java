package org.hibernate.build.gradle.quarkus;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
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
		assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		assertThat( buildResult.getOutput(), containsString( "quarkus-hibernate-orm" ) );
		assertThat( buildResult.getOutput(), containsString( "quarkus-jdbc-derby" ) );
		assertThat( buildResult.getOutput(), containsString( "quarkus-hibernate-validator" ) );

		assertThat( buildResult.getOutput(), not( containsString( "io.quarkus:" ) ) );
		assertThat( buildResult.getOutput(), not( containsString( "hibernate-core" ) ) );
	}

	@Test
	public void testListExtensions() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "listExtensions" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusExtensions" );
		assertThat( taskResult, notNullValue() );
		assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		assertThat( buildResult.getOutput(), containsString( "quarkus-hibernate-orm" ) );
		assertThat( buildResult.getOutput(), containsString( "quarkus-jdbc-derby" ) );
		assertThat( buildResult.getOutput(), containsString( "quarkus-hibernate-validator" ) );

		assertThat( buildResult.getOutput(), not( containsString( "io.quarkus:" ) ) );
		assertThat( buildResult.getOutput(), not( containsString( "hibernate-core" ) ) );
	}

	@Test
	public void testShowDependencies() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean", "showQuarkusDependencies", "--stacktrace" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusDependencies" );
		assertThat( taskResult, notNullValue() );
		assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-hibernate-orm" ) );
		assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-jdbc-derby" ) );
		assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-hibernate-validator" ) );

		assertThat( buildResult.getOutput(), containsString( "hibernate-core" ) );
		assertThat( buildResult.getOutput(), containsString( "caffeine-2.8.5.jar" ) );
	}

	@Test
	public void testShowLimitedDependencies() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" )
				.withArguments( "clean", "showQuarkusDependencies_hibernate-orm", "--stacktrace" );

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":showQuarkusDependencies_hibernate-orm" );
		assertThat( taskResult, notNullValue() );
		assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		assertThat( buildResult.getOutput(), containsString( "io.quarkus:quarkus-hibernate-orm" ) );

		assertThat( buildResult.getOutput(), containsString( "hibernate-core" ) );
		assertThat( buildResult.getOutput(), containsString( "caffeine-2.8.5.jar" ) );
	}

}
