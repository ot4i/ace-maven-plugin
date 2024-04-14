package ibm.maven.plugins.ace.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;

public final class ZipUtils {

	/**
	 * hide the default constructor
	 */
	private ZipUtils() {
		super();
	}

	/**
	 * Removes files from a given zip file.
	 * 
	 * @param zipFile       name of the zip file to be modified
	 * @param removePattern pattern of the files to be removed
	 * 
	 * @throws IOException
	 */
	public static void removeFiles(File zipFile, String removePattern) throws IOException {
		String zipFileName = zipFile.getName();
		File tmpFile = new File(zipFile.getCanonicalPath() + ".tmp");
		Project antProject = new Project();
		Target antTarget = new Target();
		antProject.addTarget("zip", antTarget);
		Zip zipTask = new Zip();
		zipTask.setProject(antProject);
		zipTask.setDestFile(tmpFile);
		ZipFileSet set = new ZipFileSet();
		set.setSrc(zipFile);
		set.setExcludes(removePattern);
		zipTask.addZipfileset(set);
		antTarget.addTask(zipTask);
		antTarget.execute();
		zipFile.delete();
		tmpFile.renameTo(new File(tmpFile.getParentFile(), zipFileName));
	}

	/**
	 * Extracts a file to the directory path
	 * 
	 * @param File    to extract
	 * @param destDir String, where files are extracted with directories 
	 * 
	 * @throws IOException
	 */
	public static void unpack(File _file, String _destDir) throws IOException {
		java.util.jar.JarFile jarfile = new java.util.jar.JarFile(_file);
		java.util.Enumeration<java.util.jar.JarEntry> enu = jarfile.entries();
		while (enu.hasMoreElements()) {
			java.util.jar.JarEntry je = enu.nextElement();

			java.io.File fl = new java.io.File(_destDir, je.getName());
			if (!fl.exists()) {
				fl.getParentFile().mkdirs();
				fl = new java.io.File(_destDir, je.getName());
			}
			if (je.isDirectory()) {
				continue;
			}
			java.io.InputStream is = jarfile.getInputStream(je);
			java.io.FileOutputStream fo = new java.io.FileOutputStream(fl);
			while (is.available() > 0) {
				fo.write(is.read());
			}
			fo.close();
			is.close();
		}
		jarfile.close();
	}

	/**
	 * Creates an archive from directory tree
	 * 
	 * @param sourceDirPath points to directory which will be recursive packed
	 * @param zipFilePath   a full path with filename where everything is stored
	 * 
	 * @throws IOException
	 */
	public static void pack(String sourceDirPath, String zipFilePath) throws IOException {
		Path p = Files.createFile(Paths.get(zipFilePath));
		try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
			Path pp = Paths.get(sourceDirPath);
			Files.walk(pp).filter(path -> !Files.isDirectory(path)).forEach(path -> {
				ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
				try {
					zs.putNextEntry(zipEntry);
					Files.copy(path, zs);
					zs.closeEntry();
				} catch (IOException e) {
					System.err.println(e);
				}
			});
		}
	}

}
