package com.github.sebersole.gradle.quarkus.service;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.dependency.DependencyService;
import com.github.sebersole.gradle.quarkus.extension.ExtensionService;
import com.github.sebersole.gradle.quarkus.indexing.IndexingService;

/**
 * Access to standard Quarkus plugin services as well as any additionally registered ones
 */
public class Services {
	private final BuildDetails buildDetails;

	private final ProjectService projectService;
	private final IndexingService indexingService;
	private final DependencyService dependencyService;

	private final ExtensionService extensionService;

	private Map<Class<?>, Object> additionalServices;

	public Services(Project project) {
		this.buildDetails = new BuildDetails( project, this );

		this.projectService = new ProjectService( buildDetails );
		this.indexingService = new IndexingService( this );
		this.dependencyService = new DependencyService( this );

		this.extensionService = new ExtensionService( this );
	}

	public BuildDetails getBuildDetails() {
		return buildDetails;
	}

	public ProjectService getProjectService() {
		return projectService;
	}

	public IndexingService getIndexingService() {
		return indexingService;
	}

	public DependencyService getDependencyService() {
		return dependencyService;
	}

	public ExtensionService getExtensionService() {
		return extensionService;
	}

	public <T> void registerService(Service<T> service) {
		if ( additionalServices == null ) {
			additionalServices = new HashMap<>();
		}
		additionalServices.put( service.getRole(), service );
	}

	@SuppressWarnings( "unchecked" )
	public <T, S extends Service<T>> S findService(Class<S> role) {
		if ( role.isAssignableFrom( ProjectService.class ) ) {
			return (S) projectService;
		}

		if ( role.isAssignableFrom( IndexingService.class ) ) {
			return (S) indexingService;
		}

		if ( role.isAssignableFrom( DependencyService.class ) ) {
			return (S) dependencyService;
		}

		if ( role.isAssignableFrom( ExtensionService.class ) ) {
			return (S) extensionService;
		}

		if ( additionalServices != null ) {
			return (S) additionalServices.get( role );
		}

		return null;
	}

	public <T, S extends Service<T>> S getService(Class<S> role) {
		final S service = findService( role );
		if ( service == null ) {
			throw new GradleException( "Unknown service role : " + role.getName() );
		}
		return service;
	}
}
