package org.hibernate.build.gradle.quarkus;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import org.junit.jupiter.api.Test;

/**
 * @author Steve Ebersole
 */
public class SimpleTest {

	@Test
	public void simpleTest() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner( "simple" );

		gradleRunner.build();
	}

}
