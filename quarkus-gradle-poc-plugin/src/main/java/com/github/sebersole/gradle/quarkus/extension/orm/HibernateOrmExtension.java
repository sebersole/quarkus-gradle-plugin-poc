package com.github.sebersole.gradle.quarkus.extension.orm;

import java.io.Serializable;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusConfigException;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.extension.AbstractExtension;
import com.github.sebersole.gradle.quarkus.extension.Artifact;
import com.github.sebersole.gradle.quarkus.extension.TransitiveExtension;
import com.github.sebersole.gradle.quarkus.task.ShowPersistenceUnitsTask;
import groovy.lang.Closure;

/**
 * Hibernate ORM specific "extension config" object
 *
 * @author Steve Ebersole
 */
public class HibernateOrmExtension extends AbstractExtension implements Serializable {
	public static final String CONTAINER_NAME = "hibernateOrm";
	public static final String ARTIFACT_SHORT_NAME = "hibernate-orm";
	public static final String DEPLOYMENT_ARTIFACT_SHORT_NAME = "hibernate-orm-deployment";

	private SupportedDatabaseFamily appliedFamily;

	private final NamedDomainObjectContainer<PersistenceUnitConfig> persistenceUnits;

	public HibernateOrmExtension(QuarkusDslImpl quarkusDsl) {
		super(
				CONTAINER_NAME,
				(extension, quarkusDsl1) -> new Artifact(
						Helper.groupArtifactVersion(
								Helper.QUARKUS_GROUP,
								Helper.QUARKUS + "-" + ARTIFACT_SHORT_NAME,
								quarkusDsl.getQuarkusVersion()
						)
				),
				(extension, quarkusDsl1) -> new Artifact(
						Helper.groupArtifactVersion(
								Helper.QUARKUS_GROUP,
								Helper.QUARKUS + "-" + DEPLOYMENT_ARTIFACT_SHORT_NAME,
								quarkusDsl.getQuarkusVersion()
						)
				),
				quarkusDsl
		);

		final Project project = quarkusDsl.getProject();

		this.persistenceUnits = project.container(
				PersistenceUnitConfig.class,
				name -> new PersistenceUnitConfig( name, quarkusDsl )
		);

		ShowPersistenceUnitsTask.from( this, quarkusDsl );

		project.afterEvaluate(
				p -> {
					if ( appliedFamily == null ) {
						throw new QuarkusConfigException( "No database-family was specified for hibernate-orm extension" );
					}

					final String familyGav = Helper.groupArtifactVersion(
							Helper.QUARKUS_GROUP,
							appliedFamily.getArtifactId(),
							quarkusDsl.getQuarkusVersion()
					);

					quarkusDsl.getBuildState().locateExtensionByGav(
							familyGav,
							() -> {
								// create one
								final TransitiveExtension transitiveExtension = new TransitiveExtension(
										appliedFamily.getArtifactId(),
										familyGav,
										quarkusDsl
								);

								quarkusDsl.getQuarkusExtensions().add( transitiveExtension );

								project.getDependencies().add(
										transitiveExtension.getRuntimeDependencies().getName(),
										transitiveExtension.getArtifact().getDependency()
								);

								// force the dependency resolution
								transitiveExtension.getRuntimeDependencies().getResolvedConfiguration();
								transitiveExtension.getDeploymentDependencies().getResolvedConfiguration();

								return transitiveExtension;
							}
					);

					persistenceUnits.forEach(
							persistenceUnitConfig -> persistenceUnitConfig.getDependencies().getResolvedConfiguration()
					);
				}
		);
	}

	public String getDatabaseFamily() {
		return appliedFamily == null ? null : appliedFamily.name();
	}

	public void setDatabaseFamily(String familyName) {
		databaseFamily( familyName );
	}

	public void databaseFamily(String familyName) {
		appliedFamily = SupportedDatabaseFamily.fromName( familyName );
	}

	public void persistenceUnits(Closure<NamedDomainObjectContainer<PersistenceUnitConfig>> closure) {
		ConfigureUtil.configure( closure, persistenceUnits );
	}

	public void persistenceUnits(Action<NamedDomainObjectContainer<PersistenceUnitConfig>> action) {
		action.execute( persistenceUnits );
	}

	public List<PersistenceUnit> resolvePersistenceUnits() {
		return PersistenceUnitResolver.resolve( persistenceUnits, quarkusDsl() );
	}
}
