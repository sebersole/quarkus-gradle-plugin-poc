package com.github.sebersole.gradle.quarkus.dsl;

import java.util.Locale;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.service.BuildDetails;
import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusConfigException;
import com.github.sebersole.gradle.quarkus.dependency.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.dependency.StandardModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.extension.Extension;
import com.github.sebersole.gradle.quarkus.extension.ExtensionCreationSupport;
import com.github.sebersole.gradle.quarkus.extension.ExtensionService;
import com.github.sebersole.gradle.quarkus.extension.ImplicitExtension;
import com.github.sebersole.gradle.quarkus.jpa.PersistenceUnitService;
import com.github.sebersole.gradle.quarkus.service.Services;
import com.github.sebersole.gradle.quarkus.task.AugmentationTask;
import com.github.sebersole.gradle.quarkus.task.ShowPersistenceUnitsTask;
import groovy.lang.Closure;

/**
 * Hibernate ORM specific ExtensionConfig providing support for configuring JPA persistence units
 */
@SuppressWarnings( "UnstableApiUsage" )
public class HibernateOrmExtensionConfig extends AbstractExtensionConfig {
	public static final String CONTAINER_NAME = "hibernateOrm";
	public static final String ARTIFACT_NAME = Helper.QUARKUS + "-hibernate-orm";

	private final Property<String> databaseFamily;
	private final NamedDomainObjectContainer<PersistenceUnitConfig> persistenceUnits;

	public HibernateOrmExtensionConfig(String name, BuildDetails buildDetails) {
		super(
				CONTAINER_NAME,
				buildDetails,
				Helper.groupArtifactVersion(
						Helper.QUARKUS_GROUP,
						ARTIFACT_NAME,
						buildDetails.getQuarkusVersion()
				)
		);

		if ( ! CONTAINER_NAME.equals( name ) ) {
			Logging.LOGGER.debug( "Registering Hibernate ORM extension under multiple names : {}", name );
		}

		final Project mainProject = buildDetails.getMainProject();
		final ObjectFactory objectFactory = mainProject.getObjects();

		this.databaseFamily = objectFactory.property( String.class );
		// todo : do we want a "default"?  e.g.
		//this.databaseFamily.convention( "h2" );

		this.persistenceUnits = objectFactory.domainObjectContainer(
				PersistenceUnitConfig.class,
				(unitName) -> {
					final PersistenceUnitConfig persistenceUnitConfig = new PersistenceUnitConfig( unitName, buildDetails );
					getRuntimeDependencies().extendsFrom( persistenceUnitConfig.getDependencies() );
					return persistenceUnitConfig;
				}
		);

		ShowPersistenceUnitsTask.from( buildDetails.getServices() );
	}

	@SuppressWarnings( { "unused", "RedundantSuppression" } )
	public Property<String> getDatabaseFamily() {
		return databaseFamily;
	}

	public void setDatabaseFamily(String family) {
		this.databaseFamily.set( family );
	}

	@SuppressWarnings( { "unused", "RedundantSuppression" } )
	public void persistenceUnits(Closure<NamedDomainObjectContainer<PersistenceUnitConfig>> closure) {
		ConfigureUtil.configure( closure, persistenceUnits );
	}

	@SuppressWarnings( { "unused", "RedundantSuppression" } )
	public void persistenceUnits(Action<NamedDomainObjectContainer<PersistenceUnitConfig>> action) {
		action.execute( persistenceUnits );
	}

	@Override
	public Extension convert(Services services) {
		final SupportedDatabaseFamily appliedDatabaseFamily = SupportedDatabaseFamily.from( databaseFamily );
		if ( appliedDatabaseFamily == null ) {
			throw new QuarkusConfigException( "No database-family was specified for hibernate-orm extension" );
		}

		final Extension extension = super.convert( services );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// handle databaseFamily

		final ModuleVersionIdentifier familyDependencyIdentifier = new StandardModuleVersionIdentifier(
				Helper.QUARKUS_GROUP,
				appliedDatabaseFamily.getArtifactId(),
				services.getBuildDetails().getQuarkusVersion()
		);

		final ExtensionService extensionService = services.getExtensionService();
		final Extension existing = extensionService.findByModule( familyDependencyIdentifier );
		if ( existing == null ) {
			//create it
			final ResolvedDependency familyRuntimeArtifact = ExtensionCreationSupport.resolveRuntimeArtifact(
					services.getBuildDetails().getMainProject().getDependencies().create( familyDependencyIdentifier.groupArtifactVersion() ),
					getRuntimeDependencies(),
					services
			);

			final ImplicitExtension implicitExtension = ImplicitExtension.from( familyDependencyIdentifier, familyRuntimeArtifact, services );
			services.getExtensionService().registerExtension( implicitExtension );
			ExtensionCreationSupport.resolveDependencies( implicitExtension, services );
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// handle persistence-units
		//		- todo : this is probably better modeled as a Task.  it is more natural.
		//			also... I am thinking to model some of these concepts as `Buildable` which
		//			would require Tasks (for builtBy + TaskDependency)

		final Project mainProject = services.getBuildDetails().getMainProject();
		final Task augmentationTask = mainProject.getTasks().getByName( AugmentationTask.REGISTRATION_NAME );
		augmentationTask.doLast(
				task -> PersistenceUnitService.from( persistenceUnits, services )
		);

		return extension;
	}

	/**
	 * NamedDomainObjectFactory implementation for the Hibernate ORM extension
	 * providing (delayed) singleton access
	 */
	public static class Factory implements NamedDomainObjectFactory<HibernateOrmExtensionConfig> {
		private final BuildDetails buildDetails;
		private HibernateOrmExtensionConfig singleton;

		public Factory(BuildDetails buildDetails) {
			this.buildDetails = buildDetails;
		}

		@Override
		public HibernateOrmExtensionConfig create(String dslName) {
			if ( singleton == null ) {
				singleton = new HibernateOrmExtensionConfig( dslName, buildDetails );
			}

			return singleton;
		}
	}

	/**
	 * Enumeration of Quarkus-supported database families
	 */
	public enum SupportedDatabaseFamily {
		DERBY( "derby" ),
		H2( "h2" );

		private final String simpleName;
		private final String artifactId;
		private final String jdbcUrlProtocol;

		SupportedDatabaseFamily(String simpleName) {
			this.simpleName = simpleName;
			this.artifactId = "quarkus-jdbc-" + simpleName;
			this.jdbcUrlProtocol = "jdbc:" + simpleName + ":";
		}

		public static SupportedDatabaseFamily from(Property<String> databaseFamily) {
			final String databaseFamilyName = databaseFamily.getOrNull();
			if ( databaseFamilyName == null ) {
				return null;
			}
			return fromName( databaseFamilyName );
		}

		public String getSimpleName() {
			return simpleName;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public String getJdbcUrlProtocol() {
			return jdbcUrlProtocol;
		}

		public static SupportedDatabaseFamily fromName(String name) {
			if ( DERBY.name().equals( name.toUpperCase( Locale.ROOT ) ) ) {
				return DERBY;
			}

			if ( H2.name().equals( name.toUpperCase( Locale.ROOT ) ) ) {
				return H2;
			}

			return null;
		}

		public static SupportedDatabaseFamily extractFromUrl(String url) {
			final SupportedDatabaseFamily[] families = SupportedDatabaseFamily.values();
			//noinspection ForLoopReplaceableByForEach
			for ( int i = 0; i < families.length; i++ ) {
				if ( url.startsWith( families[ i ].jdbcUrlProtocol ) ) {
					return families[ i ];
				}
			}

			return null;
		}
	}

}
