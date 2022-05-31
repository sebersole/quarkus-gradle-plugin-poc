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

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency;

import static com.github.sebersole.gradle.quarkus.Helper.JANDEX_INDEX_FILE_PATH;

/**
 * Useful functions for dealing with Jandex
 */
public class JandexHelper {
	public static final String JANDEX = "jandex";

	private JandexHelper() {
		// disallow direct instantiation
	}

	public static String indexFileName(ResolvedDependency dependency) {
		return indexFileNameBase( dependency ) + ".idx";
	}

	private static String indexFileNameBase(ResolvedDependency dependency) {
		if ( dependency instanceof com.github.sebersole.gradle.quarkus.dependency.ProjectDependency ) {
			return dependency.getArtifactName();
		}
		else if ( dependency instanceof com.github.sebersole.gradle.quarkus.dependency.ExternalDependency ) {
			return dependency.getGroupName() + "___" + dependency.getArtifactName() + "___" + dependency.getVersion();
		}

		throw new UnsupportedOperationException();
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


	public static Index resolveIndexFromArchive(JarFile jarFile, File jarFileFile) {
		final ZipEntry entry = jarFile.getEntry( JANDEX_INDEX_FILE_PATH );
		if ( entry != null ) {
			// the archiveFile contained a Jandex index file, use it
			return readJandexIndex( entry, jarFile, jarFileFile );
		}

		// otherwise, create an index from the artifact
		return createJandexIndex( jarFile, jarFileFile );
	}

	private static Index createJandexIndex(JarFile jarFile, File jarFileFile) {
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
							"Unable to index archive entry (`{}`) from archive (`{}`)",
							jarEntry.getRealName(),
							jarFileFile.getAbsolutePath()
					);
				}
			}
		}

		return indexer.complete();
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

	/**
	 * Write an Index to the specified File
	 */
	public static void writeIndexToFile(File outputFile, Index jandexIndex) {
		try {
			final boolean deleted = outputFile.delete();
			if ( ! deleted ) {
				Logging.LOGGER.debug( "Unable to delete index file : {}", outputFile.getAbsolutePath() );
			}
			Helper.ensureFileExists( outputFile );

			try ( final FileOutputStream out = new FileOutputStream( outputFile ) ) {
				final IndexWriter indexWriter = new IndexWriter( out );
				indexWriter.write( jandexIndex );
			}
		}
		catch ( IOException e ) {
			Logging.LOGGER.debug( "Unable to create Jandex index file {} : {}", outputFile.getAbsolutePath(), e.getMessage() );
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Used for project indexing

	public static void applyDirectory(File directory, Indexer indexer) {
		if ( ! directory.exists() ) {
			Logging.LOGGER.debug( "Skipping indexing of directory because it does not exist : {}", directory.getAbsolutePath() );
		}

		internalApplyDirectory( directory, indexer );
	}

	private static void internalApplyDirectory(File directory, Indexer indexer) {
		if ( ! directory.exists() ) {
			return;
		}

		if ( ! directory.isDirectory() ) {
			throw new GradleException( "Directory to apply to Indexer was not a directory : " + directory.getAbsolutePath() );
		}

		final File[] elements = directory.listFiles();
		if ( elements == null ) {
			Logging.LOGGER.debug( "Skipping indexing of directory because it is empty : {}", directory.getAbsolutePath() );
			return;
		}

		for ( int i = 0; i < elements.length; i++ ) {
			if ( elements[ i ].isFile() ) {
				if ( ! elements[ i ].getName().endsWith( ".class" ) ) {
					continue;
				}

				applyClassFile( elements[ i ], indexer );
			}
			else if ( elements[ i ].isDirectory() ) {
				internalApplyDirectory( elements[ i ], indexer );
			}
		}
	}

	private static void applyClassFile(File file, Indexer indexer) {
		assert file.isFile();
		assert file.exists();

		try ( final FileInputStream inputStream = new FileInputStream( file ) ) {
			indexer.index( inputStream );
		}
		catch (FileNotFoundException e) {
			// will never happen since we've already verified that the file exists
		}
		catch (IOException e) {
			throw new GradleException( "Unable apply class file to index : " + file.getAbsolutePath(), e );
		}
	}
}
