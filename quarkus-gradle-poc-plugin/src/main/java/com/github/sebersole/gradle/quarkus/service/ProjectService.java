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
	private final ProjectInfo mainProject;
	private final Map<String, ProjectInfo> subProjectByName = new HashMap<>();

	public ProjectService(Project mainProject) {
		this.mainProject = new ProjectInfo(
				mainProject.getPath(),
				mainProject.getGroup().toString(),
				mainProject.getName(),
				mainProject.getVersion().toString(),
				mainProject.getLayout().getProjectDirectory(),
				mainProject.getConvention().findPlugin( JavaPluginConvention.class )
		);

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
		this( buildDetails.getMainProject() );
	}

	@Override
	public Class<ProjectService> getRole() {
		return ProjectService.class;
	}

	public ProjectInfo getMainProject() {
		return mainProject;
	}

	public void visitSubProjects(Consumer<ProjectInfo> subProjectConsumer) {
		subProjectByName.forEach(
				(name, subProjectInfo) -> subProjectConsumer.accept( subProjectInfo )
		);
	}

	public void visitAllProjects(Consumer<ProjectInfo> subProjectConsumer) {
		subProjectConsumer.accept( mainProject );
		visitSubProjects( subProjectConsumer );
	}

	public ProjectInfo findProjectInfoByName(String name) {
		return firstFromProjects( projectInfo -> projectInfo.getArtifactName().equals( name ) );
	}

	private ProjectInfo firstFromProjects(Predicate<ProjectInfo> matcher) {
		if ( matcher.test( mainProject ) ) {
			return mainProject;
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

		if ( mainProject.getModuleIdentifier().getArtifactName().equals( identifier.getArtifactName() ) ) {
			return mainProject;
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
