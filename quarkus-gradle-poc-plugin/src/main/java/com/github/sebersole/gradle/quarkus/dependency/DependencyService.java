package com.github.sebersole.gradle.quarkus.dependency;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.service.ProjectInfo;
import com.github.sebersole.gradle.quarkus.service.Service;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Service for handling ResolvedDependency
 */
public class DependencyService implements Service<DependencyService>, Serializable {
	private final Services services;

	private final Map<ModuleVersionIdentifier, ResolvedDependency> resolvedDependencies = new HashMap<>();

	@Inject
	public DependencyService(Services services) {
		this.services = services;

		final ProjectInfo mainProjectInfo = services.getProjectService().getMainProject();
		final ProjectDependency projectDependency = new ProjectDependency(
				mainProjectInfo,
				resolvedDependency -> {
					if ( mainProjectInfo.getMainSourceSet() == null ) {
						return null;
					}
					return services.getIndexingService().registerArtifactToIndex( resolvedDependency );
				}
		);

		resolvedDependencies.put( mainProjectInfo.getModuleVersionIdentifier(), projectDependency );
	}

	@Override
	public Class<DependencyService> getRole() {
		return DependencyService.class;
	}

	public ResolvedDependency resolveDependency(ModuleVersionIdentifier identifier, Function<ModuleVersionIdentifier,ResolvedDependency> creator) {
		return resolvedDependencies.computeIfAbsent( identifier, creator );
	}

	public ResolvedDependency registerExternalDependency(org.gradle.api.artifacts.ResolvedDependency resolvedDependency) {
		final ModuleVersionIdentifier moduleVersionIdentifier = new StandardModuleVersionIdentifier(
				resolvedDependency.getModuleGroup(),
				resolvedDependency.getModuleName(),
				resolvedDependency.getModuleVersion()
		);

		final Set<ResolvedArtifact> moduleArtifacts = resolvedDependency.getModuleArtifacts();
		final ResolvedArtifact resolvedArtifact = Helper.extractOnlyOne(
				moduleArtifacts,
				() -> {},
				() -> {}
		);
		assert resolvedArtifact != null;

		final ResolvedDependency dependency = new ExternalDependency(
				moduleVersionIdentifier,
				resolvedArtifact.getFile(),
				resolvedDependency1 -> services.getIndexingService().registerArtifactToIndex( resolvedDependency1 )
		);

		resolvedDependencies.put( moduleVersionIdentifier, dependency );

		return dependency;
	}

	public ResolvedDependency registerProjectDependency(org.gradle.api.artifacts.ProjectDependency projectDependency) {
		final ModuleVersionIdentifier moduleVersionIdentifier = new StandardModuleVersionIdentifier(
				projectDependency.getGroup(),
				projectDependency.getName(),
				projectDependency.getVersion()
		);

		final ProjectInfo projectInfo = services.getProjectService().findProjectInfo( moduleVersionIdentifier );

		final ProjectDependency dependency = new ProjectDependency(
				projectInfo,
				resolvedDependency -> services.getIndexingService().registerArtifactToIndex( resolvedDependency )
		);

		resolvedDependencies.put( moduleVersionIdentifier, dependency );

		return dependency;
	}

	public ResolvedDependency findDependency(ModuleVersionIdentifier identifier) {
		return resolvedDependencies.get( identifier );
	}
}
