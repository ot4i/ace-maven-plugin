package ibm.maven.plugins.ace.mojos;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;
import ibm.maven.plugins.ace.utils.PomXmlUtils;
import net.lingala.zip4j.ZipFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import ibm.maven.plugins.ace.generated.maven_pom.Model;

/**
 * Two tasks: - unpacks any project dependencies - writes
 * org.eclipse.m2e.core.prefs if "custom maven settings.xml" should be used
 * 
 * Implemented with help from:
 * https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * requiresDependencyResolution below is required for the unpack-dependencies
 * goal to work correctly. See
 * https://github.com/TimMoore/mojo-executor/issues/3
 */

@Mojo(name = "prepare-bar-build-workspace", requiresDependencyResolution = ResolutionScope.TEST)
public class PrepareBarBuildWorkspaceMojo extends AbstractMojo {

	/**
	 * a comma separated list of dependency types to be unpacked
	 */
	private static final String UNPACK_ace_DEPENDENCY_TYPES = "zip";
	private static final String UNPACK_ace_DEPENDENCY_SCOPE = "compile";

	/**
	 * The Maven Project Object
	 */
	@Parameter(property = "project", required = true, readonly = true)
	protected MavenProject project;

	/**
	 * The Maven Session Object
	 */
	@Parameter(property = "session", required = true, readonly = true)
	protected MavenSession session;

	/**
	 * The Maven PluginManager Object
	 */
	@Component
	protected BuildPluginManager buildPluginManager;

	/**
	 * The path of the workspace in which the projects are extracted to be built.
	 */
	@Parameter(property = "ace.workspace", defaultValue = "${project.basedir}/..", required = true)
	protected File workspace;

	/**
	 * directory to unpack all dependencies
	 */
	@Parameter(property = "ace.unpackDependenciesDirectory", defaultValue = "${project.basedir}/../dependencies/${project.artifact.artifactId}", required = true, readonly = true)
	protected File unpackDependenciesDirectory;

	/**
	 * custom maven settings.xml
	 */
	@Parameter(property = "ace.customMavenSettings", defaultValue = "")
	protected String customMavenSettings;

	public void execute() throws MojoExecutionException, MojoFailureException {

		unpackaceDependencies();

		try {
			handleCustomMavenSettings();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

	/**
	 * goal of the method is to create sharedLibs projects defined by Maven
	 * dependencies for this purpose the method performs the following tasks: -
	 * unpacks dependencies of scope "compile" and type "zip" to
	 * unpackDependencyDirectory - filter dependencies for bar files - and unpack
	 * them to unpackBarDirectory - filter unpacked bar files for sharedLibs and
	 * unpack them to the workspace directory - create a .project file for the
	 * sharedLibs projects
	 * 
	 * @throws MojoExecutionException If an exception occurs
	 */
	private void unpackaceDependencies() throws MojoExecutionException {

		// define the directory to be unpacked into and create it
		workspace.mkdirs();

		// step 1:
		// unpack all dependencies that match the given scope; target:
		// unpackDependencyDirectory
		executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("2.8")),
				goal("unpack-dependencies"),
				configuration(element(name("outputDirectory"), unpackDependenciesDirectory.getAbsolutePath()),
						element(name("includeTypes"), UNPACK_ace_DEPENDENCY_TYPES),
						element(name("includeScope"), UNPACK_ace_DEPENDENCY_SCOPE)),
				executionEnvironment(project, session, buildPluginManager));

		try {

			// step 2: unpack all source files
			if (unpackDependenciesDirectory.exists()) {
				List<File> sourceFiles = FileUtils.getFiles(unpackDependenciesDirectory, "*sources.jar", "default.jar");
				for (File sourceFile : sourceFiles) {
					String[] fileParts = (FileUtils.removeExtension(sourceFile.getName())).split("-");
					String projectName = fileParts[0];
					getLog().info("found source for project: " + projectName);

					// define target environment and unpack sources
					String targetDirectory = workspace.getAbsolutePath().toString() + "/" + projectName;
					File projectDirectory = new File(targetDirectory);

					new ZipFile(sourceFile).extractAll(projectDirectory.getAbsolutePath());
					getLog().info("unpacking " + sourceFile.getName() + " to "
							+ projectDirectory.getAbsolutePath().toString());
				}
			} else {
				getLog().info("unpack dependency directory does not exist");
			}
			/*
			 * // step 3: unpack all sharedlibs - and unpack them directly to the workspace
			 * if (unpackBarDirectory.exists()) { List<File> sharedLibs =
			 * FileUtils.getFiles(unpackBarDirectory, "*.shlibzip", "default.shlibzip"); for
			 * (File sharedLib : sharedLibs) { String projectName =
			 * FileUtils.removeExtension(sharedLib.getName());
			 * 
			 * // determine the targetDirectory String targetDirectory =
			 * workspace.getAbsolutePath().toString() + "/" + projectName; File
			 * projectDirectory = new File(targetDirectory);
			 * 
			 * new ZipFile(sharedLib).extractAll(projectDirectory.getAbsolutePath());
			 * getLog().info("unpacking " + sharedLib.getName() + " to " +
			 * projectDirectory.getAbsolutePath().toString());
			 * 
			 * ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			 * InputStream inStream =
			 * classloader.getResourceAsStream("templates/project.txt");
			 * 
			 * String projectFile = IOUtils.toString(inStream); projectFile =
			 * projectFile.replace("projectname", projectName);
			 * 
			 * String targetFileName = workspace.getAbsolutePath().toString() + "/" +
			 * projectName + "/.project"; File targetFile = new File(targetFileName);
			 * 
			 * Files.write(Paths.get(targetFile.getAbsolutePath()),
			 * projectFile.getBytes(StandardCharsets.UTF_8));
			 * 
			 * } } else { getLog().info("unpack bar directory does not exist"); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * goal of the method is create the org.eclipse.m2e.core.prefs file - in the
	 * case that a "custom maven settings.xml file" is set this is required as
	 * mqsicreatebar - using Eclipse under the cover - is not aware of theses
	 * settings
	 * 
	 * @throws MojoExecutionException If an exception occurs
	 * @throws IOException
	 */
	private void handleCustomMavenSettings() throws MojoExecutionException, IOException {

		if ((customMavenSettings!=null) && (!(customMavenSettings.equalsIgnoreCase("")))) {

			getLog().info("create org.eclipse.m2e.core.prefs for custom maven settings: " + customMavenSettings);
			// customMavenSettings is set
			String targetDirectory = workspace.getAbsolutePath().toString()
					+ "/.metadata/.plugins/org.eclipse.core.runtime/.settings";
			String targetFileName = targetDirectory + "/org.eclipse.m2e.core.prefs";

			//note this would work as well 
			//however not working with templates/org.eclipse.m2e.core2.prefs
			// ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			//InputStream inStream4 = classloader.getResourceAsStream("templates/project.txt");

			
			File fdir = new File(targetDirectory);
			File fout = new File (targetFileName);
			getLog().info("creating new prefs file directory structure:"+ fdir.mkdirs());
			
			
		 
			getLog().info("creating new prefs file:"+ fout.createNewFile());
			getLog().info("start writing to file ...");
			
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			bw.write("eclipse.m2.defaultRuntime=EMBEDDED"); 
			bw.newLine();
			bw.write("eclipse.m2.runtimes=");
			bw.newLine();
			bw.write("eclipse.m2.userSettingsFile="+customMavenSettings); 
			bw.newLine();
			bw.write("eclipse.preferences.version=1");
			bw.close();
			
			
			/* 
			projectFile = projectFile.replace("settingsPath", customMavenSettings);

			File targetFile = new File(targetFileName);

			Files.write(Paths.get(targetFile.getAbsolutePath()), projectFile.getBytes(StandardCharsets.UTF_8));
			*/

		} else {
			getLog().info("using standard maven settings"); 
		}

	}

}
