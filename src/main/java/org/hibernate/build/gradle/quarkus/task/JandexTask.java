package org.hibernate.build.gradle.quarkus.task;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import org.hibernate.build.gradle.quarkus.QuarkusDsl;

import static org.hibernate.build.gradle.quarkus.Helper.QUARKUS;

/**
 * @author Steve Ebersole
 */
public class JandexTask extends DefaultTask {
	public static JandexTask createTask(QuarkusDsl dsl) {
		final JandexTask  jandexTask = dsl.getProject()
				.getTasks()
				.create( TASK_NAME, JandexTask.class, dsl );
		jandexTask.setGroup( QUARKUS );
		jandexTask.setDescription( "Generates a Jandex index in preparation for augmentation" );

		return jandexTask;
	}

	public static final String TASK_NAME = "quarkusIndex";

	private File outputDirectory;

	private Set<SourceDirectorySet> sourcesToIndex;

	@Inject
	public JandexTask(QuarkusDsl quarkusDsl) {
		final JavaPluginConvention javaPluginConvention = quarkusDsl.getProject().getConvention().findPlugin( JavaPluginConvention.class );
		final SourceSet mainSourceSet = javaPluginConvention.getSourceSets().getByName( "main" );

		this.sourcesToIndex = new HashSet<>();
		this.sourcesToIndex.add( mainSourceSet.getAllJava() );
	}

	@OutputDirectory
	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	@InputFiles
	public Set<SourceDirectorySet> getSourcesToIndex() {
		return sourcesToIndex;
	}

	public void setSourcesToIndex(Set<SourceDirectorySet> sourcesToIndex) {
		this.sourcesToIndex = sourcesToIndex;
	}

	@TaskAction
	public void generateIndex() {
		getLogger().lifecycle( "Generating Quarkus Jandex index" );
	}
}
