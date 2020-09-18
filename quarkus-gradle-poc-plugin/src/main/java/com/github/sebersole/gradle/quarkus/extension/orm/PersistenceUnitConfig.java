package com.github.sebersole.gradle.quarkus.extension.orm;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.QuarkusDsl;
import groovy.lang.Closure;

/**
 * @author Steve Ebersole
 */
public class PersistenceUnitConfig implements Named {
	private final String unitName;
	private final QuarkusDsl quarkusDsl;

	private final String configName;

	private final Configuration dependencies;

	public PersistenceUnitConfig(String unitName, QuarkusDsl quarkusDsl) {
		this.unitName = unitName;
		this.quarkusDsl = quarkusDsl;

		this.configName = determineConfigurationName( unitName );

		this.dependencies = quarkusDsl.getProject().getConfigurations().maybeCreate( configName );
	}

	public static String determineConfigurationName(String unitName) {
		return unitName + "PersistenceUnitDependencies";
	}

	@Override
	public String getName() {
		return getUnitName();
	}

	public String getUnitName() {
		return unitName;
	}

	Configuration getDependencies() {
		return dependencies;
	}

	public Dependency include(Object notation) {
		return quarkusDsl.getProject().getDependencies().add( configName, notation );
	}

	public void include(Object notation, Closure<Dependency> closure) {
		ConfigureUtil.configure( closure, include( notation ) );
	}

	public void include(Object notation, Action<Dependency> action) {
		action.execute( include( notation ) );
	}
}
