package com.github.sebersole.gradle.quarkus.dependency;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.service.ProjectInfo;
import com.github.sebersole.gradle.quarkus.QuarkusConfigException;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Helper for dealing with dependencies
 */
public class DependencyHelper {
	public static ResolvedDependency registerDependency(ModuleVersionIdentifier artifactIdentifier, ResolvedArtifact resolvedArtifact, Services services) {
		return services.getDependencyService().resolveDependency(
				artifactIdentifier,
				identifier -> {
					// Not ideal.. this makes the assumption that the determination of project / external dependency
					// can be based solely on whether the artifact File is a file or directory.  I do not think that
					// accurately covers all cases, though is fine for the normal cases of project and external
					// dependencies
					final File resolvedArtifactFile = resolvedArtifact.getFile();

					if ( resolvedArtifactFile.isFile() ) {
						return new ExternalDependency(
								artifactIdentifier,
								resolvedArtifactFile,
								resolvedDependency -> services.getIndexingService().registerArtifactToIndex( resolvedDependency )
						);
					}
					else {
						final ProjectInfo projectInfo = services.getProjectService().findProjectInfo( identifier );
						final StringBuilder registeredList = new StringBuilder( "(registered: {" );
						if ( projectInfo == null ) {
							services.getProjectService().visitAllProjects(
									projectInfo1 -> registeredList
											.append( projectInfo1.getModuleVersionIdentifier().groupArtifactVersion() )
											.append( ", " )
							);
							final String cleanedRegisteredList = registeredList.substring( 0, registeredList.length() - 2 ) + "} )";
							throw new QuarkusConfigException(
									String.format(
											Locale.ROOT,
											"Unable to locate ProjectInfo `%s` : %s", identifier, cleanedRegisteredList
									)
							);
						}
						return new ProjectDependency(
								projectInfo,
								resolvedDependency -> services.getIndexingService().registerArtifactToIndex( resolvedDependency )
						);
					}
				}
		);
	}

	public static boolean areEqual(ModuleVersionIdentifier gav1, ModuleVersionIdentifier gav2) {
		return Objects.equals( gav1.groupArtifactVersion(), gav2.groupArtifactVersion() );
	}
}
