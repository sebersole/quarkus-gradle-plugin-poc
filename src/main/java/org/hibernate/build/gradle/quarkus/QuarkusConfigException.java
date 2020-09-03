package org.hibernate.build.gradle.quarkus;

/**
 * @author Steve Ebersole
 */
public class QuarkusConfigException extends RuntimeException {
	public QuarkusConfigException(String message) {
		super( message );
	}
}
