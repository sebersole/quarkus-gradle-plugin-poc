package com.github.sebersole.gradle.quarkus.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedConfiguration;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;
import com.github.sebersole.gradle.quarkus.service.ProjectInfo;
import com.github.sebersole.gradle.quarkus.dependency.DependencyHelper;
import com.github.sebersole.gradle.quarkus.dependency.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.dependency.StandardModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.dsl.PersistenceUnitConfig;
import com.github.sebersole.gradle.quarkus.service.Service;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.jpa.PersistenceUnitResolutionSupport.applyDependency;

/**
 * Service for JPA persistence-units
 */
public class PersistenceUnitService implements Service<PersistenceUnitService> {
	public static void from(NamedDomainObjectContainer<PersistenceUnitConfig> persistenceUnitConfigs, Services services) {
		final Map<String,PersistenceUnit> persistenceUnits = new HashMap<>();

		final BuildDetails buildDetails = services.getBuildDetails();
		final Project mainProject = buildDetails.getMainProject();
		final ProjectInfo mainProjectInfo = services.getProjectService().findProjectInfoByPath( mainProject.getPath() );

		final AtomicBoolean wasMainProjectExplicitlyReferenced = new AtomicBoolean();

		persistenceUnitConfigs.forEach(
				unitConfig -> {
					assert !persistenceUnits.containsKey( unitConfig.getUnitName() );

					final PersistenceUnit persistenceUnit = new PersistenceUnit( unitConfig.getUnitName() );

					final ResolvedConfiguration resolvedExplicitInclusions = unitConfig.getDependencies().getResolvedConfiguration();
					resolvedExplicitInclusions.getResolvedArtifacts().forEach(
							resolvedArtifact -> {
								final ModuleVersionIdentifier moduleVersionIdentifier = new StandardModuleVersionIdentifier( resolvedArtifact );

								final ResolvedDependency resolvedDependency = DependencyHelper.registerDependency( moduleVersionIdentifier, resolvedArtifact, services );

								if ( DependencyHelper.areEqual( mainProjectInfo, moduleVersionIdentifier ) ) {
									wasMainProjectExplicitlyReferenced.set( true );
								}

								applyDependency( resolvedDependency, persistenceUnit, services );
							}
					);

					persistenceUnits.put( unitConfig.getUnitName(), persistenceUnit );
				}
		);

		if ( ! wasMainProjectExplicitlyReferenced.get() ) {
			final ResolvedDependency mainProjectDependency = services.getDependencyService().findDependency( mainProjectInfo );
			persistenceUnits.forEach(
					(name, persistenceUnit) -> applyDependency( mainProjectDependency, persistenceUnit, services )
			);
		}

		services.registerService( new PersistenceUnitService( persistenceUnits ) );
	}

	private final Map<String,PersistenceUnit> persistenceUnits;

	public PersistenceUnitService(Map<String,PersistenceUnit> persistenceUnits) {
		this.persistenceUnits = persistenceUnits;
	}

	@Override
	public Class<PersistenceUnitService> getRole() {
		return PersistenceUnitService.class;
	}

	public void forEach(Consumer<PersistenceUnit> consumer) {
		persistenceUnits.forEach( (name, pu) -> consumer.accept( pu ) );
	}
}
