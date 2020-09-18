package com.github.sebersole.gradle.quarkus.task;

import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import org.jboss.jandex.ClassInfo;

import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.extension.orm.HibernateOrmExtension;
import com.github.sebersole.gradle.quarkus.extension.orm.PersistenceUnit;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_BANNER_LINE;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_INDENTATION;

/**
 * @author Steve Ebersole
 */
public class ShowPersistenceUnitsTask extends DefaultTask {
	public static final String TASK_NAME = "showPersistenceUnits";

	public static ShowPersistenceUnitsTask from(HibernateOrmExtension ormExtension, QuarkusDslImpl quarkusDsl) {
		final ShowPersistenceUnitsTask task = quarkusDsl.getProject().getTasks().create(
				TASK_NAME,
				ShowPersistenceUnitsTask.class,
				ormExtension,
				quarkusDsl
		);
		task.setGroup( QUARKUS );
		task.setDescription( "Displays information about JPA persistence-units recognized by Quarkus" );

		return task;
	}

	private final HibernateOrmExtension ormExtension;
	private final QuarkusDslImpl quarkusDsl;

	@Inject
	public ShowPersistenceUnitsTask(HibernateOrmExtension ormExtension, QuarkusDslImpl quarkusDsl) {
		this.ormExtension = ormExtension;
		this.quarkusDsl = quarkusDsl;
	}

	@TaskAction
	public void showPersistenceUnits() {
		final List<PersistenceUnit> persistenceUnits = ormExtension.resolvePersistenceUnits();

		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "JPA persistence-unit information" );
		getLogger().lifecycle( REPORT_BANNER_LINE );

		persistenceUnits.forEach(
				unit -> {
					getLogger().lifecycle( "{} > Persistence Unit : {}", REPORT_INDENTATION, unit.getUnitName() );

					final Set<ClassInfo> classesToInclude = unit.getClassesToInclude();

					if ( classesToInclude == null || classesToInclude.isEmpty() ) {
						getLogger().lifecycle( "{}{} > {}", REPORT_INDENTATION, REPORT_INDENTATION, "none" );
					}
					else {
						classesToInclude.forEach(
								classInfo -> getLogger().lifecycle( "{}{} > {}", REPORT_INDENTATION, REPORT_INDENTATION, classInfo.simpleName() )
						);
					}
				}
		);
	}
}
