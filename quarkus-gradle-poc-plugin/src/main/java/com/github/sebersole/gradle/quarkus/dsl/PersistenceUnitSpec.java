package com.github.sebersole.gradle.quarkus.dsl;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;
import groovy.lang.Closure;

/**
 * Configuration for a single persistence-unit
 */
public class PersistenceUnitSpec implements Named {
	private final String unitName;
	private final BuildDetails buildDetails;

	private final String dependenciesConfigName;
	private final Configuration dependencies;

	public PersistenceUnitSpec(String unitName, BuildDetails buildDetails) {
		this.unitName = unitName;
		this.buildDetails = buildDetails;
		this.dependenciesConfigName = determineConfigurationName( unitName );
		this.dependencies = buildDetails.getMainProject().getConfigurations().maybeCreate( dependenciesConfigName );
		this.dependencies.setDescription( "Dependencies for the `" + unitName + "` JPA persistence-unit" );
	}

	private static String determineConfigurationName(String unitName) {
		return unitName + "PersistenceUnitDependencies";
	}

	@Override
	public String getName() {
		return getUnitName();
	}

	public String getUnitName() {
		return unitName;
	}

	public Configuration getDependencies() {
		return dependencies;
	}

	public Dependency include(Object notation) {
		return buildDetails.getMainProject().getDependencies().add( dependenciesConfigName, notation );
	}

	public void include(Object notation, Closure<Dependency> closure) {
		buildDetails.getMainProject().getDependencies().add( dependenciesConfigName, notation, closure );
	}

	public void include(Object notation, Action<Dependency> action) {
		action.execute( include( notation ) );
	}
}
