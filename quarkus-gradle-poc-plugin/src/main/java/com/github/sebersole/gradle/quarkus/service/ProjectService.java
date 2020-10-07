package com.github.sebersole.gradle.quarkus.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;

import com.github.sebersole.gradle.quarkus.dependency.ModuleVersionIdentifier;

/**
 * Provides access to details about the projects
 */
public class ProjectService implements Service<ProjectService> {
	private final ProjectInfo mainProjectInfo;
	private final Map<String, ProjectInfo> subProjectByName = new HashMap<>();

	public ProjectService(Project mainProject, ProjectInfo mainProjectInfo) {
		this.mainProjectInfo = mainProjectInfo;
		mainProject.subprojects(
				project -> {
					final ProjectInfo subProjectInfo = new ProjectInfo(
							project.getPath(),
							project.getGroup().toString(),
							project.getName(),
							project.getVersion().toString(),
							project.getLayout().getProjectDirectory(),
							project.getConvention().findPlugin( JavaPluginConvention.class )
					);
					subProjectByName.put( project.getName(), subProjectInfo );
				}
		);
	}

	public ProjectService(BuildDetails buildDetails) {
		this( buildDetails.getMainProject(), buildDetails.getMainProjectInfo() );
	}

	@Override
	public Class<ProjectService> getRole() {
		return ProjectService.class;
	}

	public ProjectInfo getMainProjectInfo() {
		return mainProjectInfo;
	}

	public void visitSubProjects(Consumer<ProjectInfo> subProjectConsumer) {
		subProjectByName.forEach(
				(name, subProjectInfo) -> subProjectConsumer.accept( subProjectInfo )
		);
	}

	public void visitAllProjects(Consumer<ProjectInfo> subProjectConsumer) {
		subProjectConsumer.accept( mainProjectInfo );
		visitSubProjects( subProjectConsumer );
	}

	public ProjectInfo findProjectInfoByName(String name) {
		return firstFromProjects( projectInfo -> projectInfo.getArtifactName().equals( name ) );
	}

	private ProjectInfo firstFromProjects(Predicate<ProjectInfo> matcher) {
		if ( matcher.test( mainProjectInfo ) ) {
			return mainProjectInfo;
		}

		for ( Map.Entry<String, ProjectInfo> entry : subProjectByName.entrySet() ) {
			if ( matcher.test( entry.getValue() ) ) {
				return entry.getValue();
			}
		}

		return null;
	}

	public ProjectInfo findProjectInfoByPath(String path) {
		return firstFromProjects( projectInfo -> projectInfo.getPath().equals( path ) );
	}

	public ProjectInfo findProjectInfo(ModuleVersionIdentifier identifier) {
		final ProjectInfo matched = firstFromProjects(
				projectInfo -> Objects.equals( projectInfo.getModuleVersionIdentifier(), identifier )
		);

		if ( matched != null ) {
			return matched;
		}

		if ( mainProjectInfo.getModuleIdentifier().getArtifactName().equals( identifier.getArtifactName() ) ) {
			return mainProjectInfo;
		}

		return null;
	}

	public ProjectInfo findProjectInfo(
			String groupName,
			String artifactName,
			String version) {
		return firstFromProjects(
				projectInfo -> Objects.equals( projectInfo.getModuleVersionIdentifier().getGroupName(), groupName )
						&& Objects.equals( projectInfo.getModuleVersionIdentifier().getArtifactName(), artifactName )
						&& Objects.equals( projectInfo.getModuleVersionIdentifier().getVersion(), version )

		);
	}
}
