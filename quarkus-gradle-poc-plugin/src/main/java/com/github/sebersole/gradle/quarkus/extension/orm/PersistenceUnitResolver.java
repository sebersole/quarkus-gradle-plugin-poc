package com.github.sebersole.gradle.quarkus.extension.orm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.ResolvedConfiguration;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.dependency.MutableCompositeIndex;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependencyFactory;

import static com.github.sebersole.gradle.quarkus.indexing.JandexHelper.createJandexDotName;

/**
 * @author Steve Ebersole
 */
public class PersistenceUnitResolver {
	public static final DotName JPA_PKG = createJandexDotName( "javax", "persistence" );
	public static final DotName HHH_PKG = createJandexDotName( "org", "hibernate", "annotations" );

	public static final DotName JPA_ENTITY = DotName.createComponentized( JPA_PKG, "Entity" );
	public static final DotName JPA_EMBEDDABLE = DotName.createComponentized( JPA_PKG, "Embeddable" );
	public static final DotName JPA_EMBEDDED = DotName.createComponentized( JPA_PKG, "Embedded" );
	public static final DotName JPA_EMBEDDED_ID = DotName.createComponentized( JPA_PKG, "EmbeddedId" );
	public static final DotName JPA_CONVERTER_ANN = DotName.createComponentized( JPA_PKG, "Converter" );
	public static final DotName JPA_CONVERTER = DotName.createComponentized( JPA_PKG, "AttributeConverter" );

	public static final DotName HHH_ENTITY = DotName.createComponentized( HHH_PKG, "Entity" );

	static List<PersistenceUnit> resolve(
			Collection<PersistenceUnitConfig> unitConfigs,
			QuarkusDslImpl quarkusDsl) {

		final Map<String,PersistenceUnit> resolvedPersistenceUnits = new HashMap<>();
		final Map<String, Set<String>> unitNamesByExplicitGav = new HashMap<>();

		// as a "preliminary" step... go through the pu specific dependencies and

		// first resolve the directly defined PU entries...
		//		- this drives some of the later steps
		unitConfigs.forEach(
				unitConfig -> {
					assert ! resolvedPersistenceUnits.containsKey( unitConfig.getUnitName() );

					final PersistenceUnit persistenceUnit = new PersistenceUnit( unitConfig.getUnitName(), quarkusDsl );

					resolvedPersistenceUnits.put( persistenceUnit.getUnitName(), persistenceUnit );

					final ResolvedConfiguration resolvedExplicitInclusions = unitConfig.getDependencies().getResolvedConfiguration();
					resolvedExplicitInclusions.getResolvedArtifacts().forEach(
							resolvedArtifact -> {
								final String gav = Helper.groupArtifactVersion( resolvedArtifact );
								final ResolvedDependency resolvedDependency = quarkusDsl.getBuildState().findResolvedDependency( gav );
								if ( resolvedDependency == null ) {
									throw new GradleException( "Unable to locate ResolvedDependency(" + gav + ")" );
								}

								applyDependency( resolvedDependency, persistenceUnit, quarkusDsl );
								final Set<String> unitNames = unitNamesByExplicitGav.computeIfAbsent(
										resolvedDependency.getGav(),
										(s) -> new HashSet<>()
								);

								unitNames.add( persistenceUnit.getUnitName() );
							}
					);
				}
		);

		// at this point all of the explicit/direct inclusions have been processed.  Now we want to
		// process all resolved dependencies to see if they include any JPA "managed classes".
		//
		// we want to be careful however to not process:
		//		1) any that are already included in the PU : `unitNamesByExplicitGav.get( gav ).contains( unitName )`
		//		2) any that were explicitly defined as part of another PU : `unitNamesByExplicitGav.get( gav ).contains( unitName )`

		final ResolvedConfiguration resolvedConfiguration = quarkusDsl.getRuntimeDependencies().getResolvedConfiguration();

		resolvedConfiguration.getResolvedArtifacts().forEach(
				resolvedArtifact -> {
					final String gav = Helper.groupArtifactVersion( resolvedArtifact );
					final Set<String> unitsIncludingGav = unitNamesByExplicitGav.get( gav );
					final ResolvedDependency resolvedDependency = quarkusDsl.getBuildState().findResolvedDependency( gav );

					resolvedPersistenceUnits.forEach(
							(unitName, unit) -> {
								if ( unitsIncludingGav == null || unitsIncludingGav.isEmpty() ) {
									// no units explicitly named that dependency - add it to each
									applyDependency( resolvedDependency, unit, quarkusDsl );
								}
							}
					);

				}
		);

		return new ArrayList<>( resolvedPersistenceUnits.values() );
	}


	private static void applyDependency(
			ResolvedDependency resolvedDependency,
			PersistenceUnit unit,
			QuarkusDslImpl quarkusDsl) {
		if ( resolvedDependency == null ) {
			return;
		}

		final MutableCompositeIndex compositeJandexIndex = quarkusDsl.getBuildState().getResolvedCompositeIndex();
		final IndexView jandexIndex = resolvedDependency.getJandexIndexAccess().get();

		// first look for things which have identifying annotations on the classes...
		applyFromClass(
				jandexIndex,
				unit,
				compositeJandexIndex,
				JPA_ENTITY,
				JPA_CONVERTER_ANN,
				JPA_EMBEDDABLE,
				HHH_ENTITY
		);

		// look for annotations marking fields or methods whose (return) type should also be managed
		consumeFromTargetType(
				jandexIndex,
				unit,
				compositeJandexIndex,
				JPA_EMBEDDED,
				JPA_EMBEDDED_ID
		);

		// look for implementors of special contracts identifying managed types
		consumeImplementors(
				jandexIndex,
				unit,
				JPA_CONVERTER
		);
	}

	private static void applyFromClass(
			IndexView jandexIndex,
			PersistenceUnit unit,
			IndexView compositeJandexIndex,
			DotName... annotationNames) {
		for ( int i = 0; i < annotationNames.length; i++ ) {
			final Collection<AnnotationInstance> uses = jandexIndex.getAnnotationsWithRepeatable( annotationNames[i], compositeJandexIndex );
			uses.forEach(
					usage -> {
						final AnnotationTarget target = usage.target();
						// can only happen on classes...
						assert target instanceof ClassInfo;
						collectClass( (ClassInfo) target, unit );
					}
			);
		}
	}

	private static void consumeFromTargetType(
			IndexView jandexIndex,
			PersistenceUnit unit,
			IndexView compositeJandexIndex,
			DotName... annotationNames) {
		for ( int i = 0; i < annotationNames.length; i++ ) {
			final Collection<AnnotationInstance> uses = jandexIndex.getAnnotations( annotationNames[ i ] );
			uses.forEach(
					usage -> {
						final AnnotationTarget target = usage.target();
						final DotName referencedClassName;

						// target needs to be either a field or method (getter)

						if ( target instanceof FieldInfo ) {
							final FieldInfo fieldInfo = (FieldInfo) target;
							referencedClassName = fieldInfo.type().name();
						}
						else if ( target instanceof MethodInfo ) {
							final MethodInfo methodInfo = (MethodInfo) target;
							final String methodName = methodInfo.name();
							assert methodName.startsWith( "get" );
							referencedClassName = methodInfo.returnType().name();
						}
						else {
							throw new GradleException(
									"Unexpected AnnotationInstance target type `" + target.kind() + "`; expecting METHOD or FIELD"
							);
						}

						final ClassInfo referencedClassInfo = compositeJandexIndex.getClassByName( referencedClassName );
						if ( referencedClassInfo == null ) {
							Logging.LOGGER.debug( "Could not locate referenced class type : {}", referencedClassName.toString() );
						}
						else {
							collectClass( referencedClassInfo, unit );
						}
					}
			);
		}
	}

	private static void consumeImplementors(
			IndexView jandexIndex,
			PersistenceUnit unit,
			DotName... typeNames) {
		for ( int i = 0; i < typeNames.length; i++ ) {
			unit.applyClassesToInclude( jandexIndex.getAllKnownImplementors( typeNames[i] ) );
		}
	}

	private static void collectClass(ClassInfo classInfo, PersistenceUnit unit) {
		unit.applyClassToInclude( classInfo );
	}
}
