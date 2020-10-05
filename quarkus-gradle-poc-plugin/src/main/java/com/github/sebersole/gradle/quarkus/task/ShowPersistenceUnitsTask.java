package com.github.sebersole.gradle.quarkus.task;

import java.util.Set;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;

import org.jboss.jandex.ClassInfo;

import com.github.sebersole.gradle.quarkus.jpa.PersistenceUnit;
import com.github.sebersole.gradle.quarkus.jpa.PersistenceUnitService;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_BANNER_LINE;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_INDENTATION;

/**
 * Task for showing all managed persistence-units
 */
public class ShowPersistenceUnitsTask extends DefaultTask {
	public static final String REGISTRATION_NAME = "showPersistenceUnits";

	public static ShowPersistenceUnitsTask from(Services services) {
		final ShowPersistenceUnitsTask task = services.getBuildDetails().getMainProject().getTasks().create(
				REGISTRATION_NAME,
				ShowPersistenceUnitsTask.class,
				services
		);
		task.setGroup( QUARKUS );
		task.setDescription( "Displays information about JPA persistence-units managed by Quarkus" );

		final Task augmentationTask = services.getBuildDetails().getMainProject().getTasks().getByName( AugmentationTask.REGISTRATION_NAME );
		task.dependsOn( augmentationTask );

		final Configuration implementationDependencies = services.getBuildDetails().getMainProject().getConfigurations().getByName( "implementation" );
		// a strange bit of Gradle showing through.  we cannot resolve `implementation` ourselves.  Instead we need
		// to create a new Configuration that "extends" `implementation` and resolve that.
		//
		// org.gradle.api.internal.tasks.TaskDependencyResolveException: Could not determine the dependencies of task ':showPersistenceUnits'.
		//	at org.gradle.api.internal.tasks.CachingTaskDependencyResolveContext.getDependencies(CachingTaskDependencyResolveContext.java:68)
		//	at org.gradle.execution.plan.TaskDependencyResolver.resolveDependenciesFor(TaskDependencyResolver.java:46)
		//	at org.gradle.execution.plan.LocalTaskNode.getDependencies(LocalTaskNode.java:161)ExecutorPolicy.java:64)
		//	...
		//Caused by: java.lang.IllegalStateException: Resolving dependency configuration 'implementation' is not allowed as it is defined as 'canBeResolved=false'.
		//Instead, a resolvable ('canBeResolved=true') dependency configuration that extends 'implementation' should be resolved.
		final Configuration resolvable = services.getBuildDetails().getMainProject().getConfigurations().detachedConfiguration();
		resolvable.extendsFrom( implementationDependencies );

		return task;
	}

	private final Services services;

	@Inject
	public ShowPersistenceUnitsTask(Services services) {
		this.services = services;
	}

	@TaskAction
	public void showPersistenceUnits() {
		services.getService( PersistenceUnitService.class ).forEach( this::showPersistenceUnit );
	}

	private void showPersistenceUnit(PersistenceUnit persistenceUnit) {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "JPA persistence-unit : {}", persistenceUnit.getUnitName() );
		getLogger().lifecycle( REPORT_BANNER_LINE );

		final Set<ClassInfo> classesToInclude = persistenceUnit.getClassesToInclude();

		if ( classesToInclude == null || classesToInclude.isEmpty() ) {
			getLogger().lifecycle( "{}{} > {}", REPORT_INDENTATION, REPORT_INDENTATION, "none" );
		}
		else {
			classesToInclude.forEach(
					classInfo -> getLogger().lifecycle( "{}{} > {}", REPORT_INDENTATION, REPORT_INDENTATION, classInfo.simpleName() )
			);
		}
	}
}
