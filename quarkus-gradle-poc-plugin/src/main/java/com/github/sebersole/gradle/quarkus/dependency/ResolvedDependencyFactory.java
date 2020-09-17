package com.github.sebersole.gradle.quarkus.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;

import static com.github.sebersole.gradle.quarkus.Helper.JANDEX_INDEX_FILE_PATH;

/**
 * @author Steve Ebersole
 */
public class ResolvedDependencyFactory {
	public static ResolvedDependency from(ResolvedArtifact resolvedArtifact, QuarkusDsl quarkusDsl) {
		final ModuleVersionIdentifier moduleVersionIdentifier = resolvedArtifact.getModuleVersion().getId();
		return new ResolvedDependency(
				moduleVersionIdentifier.getGroup(),
				moduleVersionIdentifier.getName(),
				moduleVersionIdentifier.getVersion(),
				resolvedArtifact.getFile(),
				extractJandexIndex( resolvedArtifact, quarkusDsl )
		);
	}

	private static IndexView extractJandexIndex(ResolvedArtifact resolvedArtifact, QuarkusDsl quarkusDsl) {
		final File artifactBase = resolvedArtifact.getFile();

		if ( ! artifactBase.exists() ) {
			Logging.LOGGER.debug( "Skipping indexing artifact as it's base does not physically exist : {}", artifactBase.getAbsolutePath()  );
		}

		if ( artifactBase.isDirectory() ) {
			// handle (local) project-based dependency
			return resolveIndexFromProjectDependency( artifactBase, resolvedArtifact, quarkusDsl );
		}
		else {
			// assume it is an archive
			return resolveIndexFromArchive( artifactBase, resolvedArtifact, quarkusDsl );
		}
	}

	private static Index resolveIndexFromProjectDependency(File artifactBase, ResolvedArtifact resolvedArtifact, QuarkusDsl quarkusDsl) {
		final Project matchingProject = findMatchingProject( quarkusDsl.getProject().getRootProject(), resolvedArtifact, quarkusDsl );
		if ( matchingProject == null ) {
			return null;
		}

		final JavaPluginConvention javaPluginConvention = matchingProject.getConvention().findPlugin( JavaPluginConvention.class );
		assert javaPluginConvention != null;

		final SourceSet mainSourceSet = javaPluginConvention.getSourceSets().getByName( SourceSet.MAIN_SOURCE_SET_NAME );
		final Provider<Directory> directoryProvider = mainSourceSet.getResources().getClassesDirectory();
		final Directory resourcesOutputDir = directoryProvider.getOrNull();
		assert resourcesOutputDir != null;

		final RegularFile indexFileRef = resourcesOutputDir.file( JANDEX_INDEX_FILE_PATH );
		final File indexFile = indexFileRef.getAsFile();

		if ( indexFile.exists() ) {
			// simply load the index from the file
			return loadJandexIndex( indexFile );
		}
		else {
			// create the index and write it out
			final Index jandexIndex = createJandexIndexFromDirectory( artifactBase, matchingProject, quarkusDsl );

			writeIndexToFile( indexFile, jandexIndex, matchingProject, quarkusDsl );

			return jandexIndex;
		}
	}

	private static Project findMatchingProject(Project project, ResolvedArtifact resolvedArtifact, QuarkusDsl quarkusDsl) {
		if ( project.getName().equals( resolvedArtifact.getName() ) ) {
			assert project.getPlugins().hasPlugin( JavaPlugin.class );
			assert project.getConvention().findPlugin( JavaPluginConvention.class ) != null;
			return project;
		}

		for ( Project subProject : project.getSubprojects() ) {
			final Project matchingProject = findMatchingProject( subProject, resolvedArtifact, quarkusDsl );
			if ( matchingProject != null ) {
				return matchingProject;
			}
		}

		return null;
	}

	private static Index loadJandexIndex(File jandexFile) {
		try ( final FileInputStream inputStream = new FileInputStream( jandexFile ) ) {
			final IndexReader reader = new IndexReader( inputStream );
			return reader.read();
		}
		catch ( FileNotFoundException e ) {
			throw new GradleException(
					String.format(
							Locale.ROOT,
							"`File(%s)#exists` returned true for Jandex index file, but FileNotFoundException occurred opening stream",
							jandexFile.getAbsolutePath()
					),
					e
			);
		}
		catch ( IOException e ) {
			Logging.LOGGER.debug(
					"IOException accessing Jandex index file [{}] : {}",
					jandexFile.getAbsolutePath(),
					e.getMessage()
			);
		}

		return null;
	}

	/**
	 * Generate a Jandex Index from directory (File)
	 */
	public static Index createJandexIndexFromDirectory(File directory, Project project, QuarkusDsl quarkusDsl) {
		assert directory.isDirectory();

		return createJandexIndexFromFileCollection( project.files( directory ), project, quarkusDsl );
	}

	/**
	 * Generate a Jandex Index from a FileCollection
	 */
	public static Index createJandexIndexFromFileCollection(FileCollection fileCollection, Project project, QuarkusDsl quarkusDsl) {
		final Indexer jandexIndexer = new Indexer();
		fileCollection.forEach(
				file -> {
					assert file.exists();

					if ( ! file.isDirectory() ) {
						try ( final InputStream inputStream = new FileInputStream( file ) ) {
							jandexIndexer.index( inputStream );
						}
						catch ( FileNotFoundException e ) {
							Logging.LOGGER.debug( "Unable to find input file for indexing : {}", file.getAbsolutePath() );
						}
						catch ( IOException e ) {
							Logging.LOGGER.debug( "Unable to access input file for indexing : {}", file.getAbsolutePath() );
						}
					}
				}
		);

		return jandexIndexer.complete();
	}

	/**
	 * Write an Index to the specified File
	 */
	public static void writeIndexToFile(File indexFile, Index jandexIndex, Project project, QuarkusDsl quarkusDsl) {
		try {
			Helper.ensureFileExists( indexFile, quarkusDsl );

			try ( final FileOutputStream out = new FileOutputStream( indexFile ) ) {
				final IndexWriter indexWriter = new IndexWriter( out );
				indexWriter.write( jandexIndex );
			}
		}
		catch ( IOException e ) {
			Logging.LOGGER.debug( "Unable to create Jandex index file {} : {}", indexFile.getAbsolutePath(), e.getMessage() );
		}
	}

	private static IndexView resolveIndexFromArchive(File artifactBase, ResolvedArtifact resolvedArtifact, QuarkusDsl quarkusDsl) {
		final File jandexFile = new File( artifactBase, JANDEX_INDEX_FILE_PATH );

		if ( jandexFile.exists() ) {
			return loadJandexIndex( jandexFile );
		}

		// otherwise, create it
		return createJandexIndexArchive( artifactBase, resolvedArtifact, quarkusDsl );

	}

	private static IndexView createJandexIndexArchive(
			File artifactBase,
			ResolvedArtifact resolvedArtifact,
			QuarkusDsl quarkusDsl) {
		final Indexer indexer = new Indexer();

		try ( final JarFile jarFile = new JarFile( artifactBase ) ) {
			final Enumeration<JarEntry> entries = jarFile.entries();
			while ( entries.hasMoreElements() ) {
				final JarEntry jarEntry = entries.nextElement();

				if ( jarEntry.getName().endsWith( ".class" ) ) {
					try ( final InputStream stream = jarFile.getInputStream( jarEntry ) ) {
						indexer.index( stream );
					}
					catch (Exception e) {
						Logging.LOGGER.debug( "Unable to index archive entry - {} : {}", jarEntry.getRealName(), artifactBase.getAbsolutePath() );
					}
				}
			}
		}
		catch ( IOException e ) {
			Logging.LOGGER.debug( "Unable to access dependency artifact as a directory nor JarFile : {}", artifactBase.getAbsolutePath() );
		}

		return indexer.complete();
	}
}
