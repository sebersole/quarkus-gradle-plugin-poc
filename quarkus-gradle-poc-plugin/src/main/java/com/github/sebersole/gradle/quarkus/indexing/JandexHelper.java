package com.github.sebersole.gradle.quarkus.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusDsl;
import com.github.sebersole.gradle.quarkus.QuarkusDslImpl;
import com.github.sebersole.gradle.quarkus.task.JandexTask;

import static com.github.sebersole.gradle.quarkus.Helper.JANDEX_INDEX_FILE_PATH;

/**
 * Useful functions for dealing with Jandex
 */
public class JandexHelper {
	private JandexHelper() {
		// disallow direct instantiation
	}

	/**
	 * Create a Jandex DotName from the given parts.  Always creates "componentized" names
	 */
	public static DotName createJandexDotName(String... parts) {
		assert parts != null;
		assert parts.length > 0;

		DotName result = DotName.createComponentized( null, parts[0] );

		for ( int i = 1; i < parts.length; i++ ) {
			result = DotName.createComponentized( result, parts[i] );
		}

		return result;
	}

	public static String makeUniqueName(ResolvedArtifact resolvedArtifact) {
		return Helper.sanitize( resolvedArtifact.getName() ) + "__" + Helper.sanitize( resolvedArtifact.getName() );
	}

	public static String artifactTaskName(ResolvedArtifact resolvedArtifact) {
		return JandexTask.TASK_NAME + "_" + makeUniqueName( resolvedArtifact );

	}

	public static String outputFileName(ResolvedArtifact resolvedArtifact) {
		return makeUniqueName( resolvedArtifact ) + ".idx";
	}

	/**
	 * @see IndexResolver
	 */
	public static Index resolveJandexIndex(
			String gav,
			File artifactBase,
			RegularFile outputFile,
			QuarkusDslImpl quarkusDsl) {
		Logging.LOGGER.trace( "Resolving Jandex index for dependency `{}`", gav );

		if ( ! artifactBase.exists() ) {
			Logging.LOGGER.debug(
					"Skipping indexing dependency `{}` as it's base does not physically exist : {}",
					gav,
					artifactBase.getAbsolutePath()
			);
			return null;
		}

		final Index index;
		if ( artifactBase.isDirectory() ) {
			// handle (local) project-based dependency
			index = resolveIndexFromDirectory( artifactBase, quarkusDsl );
		}
		else {
			// assume it is an archive
			index = resolveIndexFromArchive( gav, artifactBase );
		}

		if ( index == null ) {
			Logging.LOGGER.debug( "Unable to resolve Jandex index from dependency `{}`", gav );
			return null;
		}

		writeIndexToFile( outputFile.getAsFile(), index, quarkusDsl );

		return index;
	}

	private static Index resolveIndexFromDirectory(File directory, QuarkusDsl quarkusDsl) {
		assert directory.exists();

		final File indexFileLocation = new File( directory, JANDEX_INDEX_FILE_PATH );
		if ( indexFileLocation.exists() ) {
			return readJandexIndex( indexFileLocation );
		}

		// otherwise, generate it from the artifact
		return createJandexIndex( indexFileLocation, quarkusDsl );
	}

	/**
	 * Read a Jandex index file and return the "serialized" index
	 */
	public static Index readJandexIndex(File jandexFile) {
		try ( final InputStream inputStream = new FileInputStream( jandexFile ) ) {
			return readJandexIndex( () -> inputStream );
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

	private static Index readJandexIndex(Supplier<InputStream> streamAccess) throws IOException {
		final IndexReader reader = new IndexReader( streamAccess.get() );
		return reader.read();
	}

	private static Index createJandexIndex(File directory, QuarkusDsl quarkusDsl) {
		assert directory.isDirectory();

		return createJandexIndex( quarkusDsl.getProject().files( directory ) );
	}

	private static Index createJandexIndex(FileCollection fileCollection) {
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

	private static Index resolveIndexFromArchive(String gav, File artifactBase) {
		assert artifactBase.exists();

		final JarFile jarFile;
		try {
			jarFile = new JarFile( artifactBase );
		}
		catch (IOException e) {
			Logging.LOGGER.debug( "Exception trying to handle dependency artifact as a JAR" );
			return null;
		}

		final ZipEntry entry = jarFile.getEntry( JANDEX_INDEX_FILE_PATH );
		if ( entry != null ) {
			// the archive contained a Jandex index file, use it
			return readJandexIndex( entry, jarFile, artifactBase );
		}

		// otherwise, create an index from the artifact
		return createJandexIndex( gav, jarFile, artifactBase );
	}

	private static Index readJandexIndex(ZipEntry indexEntry, JarFile jarFile, File jarFileFile) {
		try ( final InputStream indexStream = jarFile.getInputStream( indexEntry ) ) {
			return readJandexIndex( () -> indexStream );
		}
		catch (FileNotFoundException e) {
			throw new GradleException(
					String.format(
							Locale.ROOT,
							"Unable to access InputStream from ZipEntry relative to `%s`",
							jarFileFile.getAbsolutePath()
					),
					e
			);
		}
		catch (IOException e) {
			Logging.LOGGER.debug(
					"IOException accessing Jandex index file [{}] : {}",
					jarFileFile.getAbsolutePath(),
					e.getMessage()
			);
		}

		return null;
	}

	private static Index createJandexIndex(String gav, JarFile jarFile, File jarFileFile) {
		final Indexer indexer = new Indexer();

		final Enumeration<JarEntry> entries = jarFile.entries();
		while ( entries.hasMoreElements() ) {
			final JarEntry jarEntry = entries.nextElement();

			if ( jarEntry.getName().endsWith( ".class" ) ) {
				try ( final InputStream stream = jarFile.getInputStream( jarEntry ) ) {
					indexer.index( stream );
				}
				catch (Exception e) {
					Logging.LOGGER.debug(
							"Unable to index archive entry (`{}`) from archive (`{}`) for {}",
							jarEntry.getRealName(),
							jarFileFile.getAbsolutePath(),
							gav
					);
				}
			}
		}

		return indexer.complete();
	}

	/**
	 * Write an Index to the specified File
	 */
	public static void writeIndexToFile(File outputFile, Index jandexIndex, QuarkusDsl quarkusDsl) {
		try {
			Helper.ensureFileExists( outputFile, quarkusDsl );

			try ( final FileOutputStream out = new FileOutputStream( outputFile ) ) {
				final IndexWriter indexWriter = new IndexWriter( out );
				indexWriter.write( jandexIndex );
			}
		}
		catch ( IOException e ) {
			Logging.LOGGER.debug( "Unable to create Jandex index file {} : {}", outputFile.getAbsolutePath(), e.getMessage() );
		}
	}
}
