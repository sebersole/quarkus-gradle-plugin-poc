package com.github.sebersole.gradle.quarkus.jpa;

import java.util.Collection;

import org.gradle.api.GradleException;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.indexing.MutableCompositeIndex;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.indexing.JandexHelper.createJandexDotName;

/**
 * @author Steve Ebersole
 */
public class PersistenceUnitResolutionSupport {
	public static final DotName JPA_PKG = createJandexDotName( "javax", "persistence" );
	public static final DotName HHH_PKG = createJandexDotName( "org", "hibernate", "annotations" );

	public static final DotName JPA_ENTITY = DotName.createComponentized( JPA_PKG, "Entity" );
	public static final DotName JPA_EMBEDDABLE = DotName.createComponentized( JPA_PKG, "Embeddable" );
	public static final DotName JPA_EMBEDDED = DotName.createComponentized( JPA_PKG, "Embedded" );
	public static final DotName JPA_EMBEDDED_ID = DotName.createComponentized( JPA_PKG, "EmbeddedId" );
	public static final DotName JPA_CONVERTER_ANN = DotName.createComponentized( JPA_PKG, "Converter" );
	public static final DotName JPA_CONVERTER = DotName.createComponentized( JPA_PKG, "AttributeConverter" );

	public static final DotName HHH_ENTITY = DotName.createComponentized( HHH_PKG, "Entity" );


	public static void applyDependency(
			ResolvedDependency resolvedDependency,
			PersistenceUnit unit,
			Services services) {
		if ( resolvedDependency == null ) {
			return;
		}

		final MutableCompositeIndex compositeIndex = services.getIndexingService().getCompositeIndex();
		final IndexView jandexIndex = resolvedDependency.getIndexAccess().getIndex();

		// first look for things which have identifying annotations on the classes...
		applyFromClass(
				jandexIndex,
				unit,
				compositeIndex,
				services,
				JPA_ENTITY,
				JPA_CONVERTER_ANN,
				JPA_EMBEDDABLE,
				HHH_ENTITY
		);

		// look for annotations marking fields or methods whose (return) type should also be managed
		consumeFromTargetType(
				jandexIndex,
				unit,
				compositeIndex,
				services,
				JPA_EMBEDDED,
				JPA_EMBEDDED_ID
		);

		// look for implementors of special contracts identifying managed types
		consumeImplementors(
				jandexIndex,
				unit,
				services,
				JPA_CONVERTER
		);
	}

	private static void applyFromClass(
			IndexView jandexIndex,
			PersistenceUnit unit,
			IndexView compositeJandexIndex,
			Services services,
			DotName... annotationNames) {
		for ( int i = 0; i < annotationNames.length; i++ ) {
			final Collection<AnnotationInstance> uses = jandexIndex.getAnnotationsWithRepeatable( annotationNames[i], compositeJandexIndex );
			uses.forEach(
					usage -> {
						final AnnotationTarget target = usage.target();
						// can only happen on classes...
						assert target instanceof ClassInfo;
						collectClass( (ClassInfo) target, unit, services );
					}
			);
		}
	}

	private static void consumeFromTargetType(
			IndexView jandexIndex,
			PersistenceUnit unit,
			IndexView compositeJandexIndex,
			Services services, DotName... annotationNames) {
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
							collectClass( referencedClassInfo, unit, services );
						}
					}
			);
		}
	}

	private static void consumeImplementors(
			IndexView jandexIndex,
			PersistenceUnit unit,
			Services services,
			DotName... typeNames) {
		for ( int i = 0; i < typeNames.length; i++ ) {
			final Collection<ClassInfo> implementors = jandexIndex.getAllKnownImplementors( typeNames[ i ] );
			implementors.forEach( classInfo -> collectClass( classInfo, unit, services ) );
		}
	}

	private static void collectClass(ClassInfo classInfo, PersistenceUnit unit, Services services) {
		services.getBuildDetails()
				.getMainProject()
				.getLogger()
				.debug( "Collecting `{}` as managed persistence-unit Class : {}", classInfo.simpleName(), unit.getUnitName() );
		unit.applyClassToInclude( classInfo );
	}
}
