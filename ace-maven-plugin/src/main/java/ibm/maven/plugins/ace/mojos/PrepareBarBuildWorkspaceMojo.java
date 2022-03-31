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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import ibm.maven.plugins.ace.generated.maven_pom.Model;

/**
 * Unpacks the dependent WebSphere Message Broker Projects.
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
	@Parameter(property = "ace.unpackDependenciesDirectory", defaultValue = "${project.build.directory}/dependencies", required = true, readonly = true)
	protected File unpackDependenciesDirectory;

	/**
	 * directory for all bar files / out of the dependencies
	 */
	@Parameter(property = "ace.unpackBarDirectory", defaultValue = "${project.build.directory}/dependencies/bars", required = true, readonly = true)
	protected File unpackBarDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		unpackaceDependencies();
	
	}



	/**
	 * @param pomFile
	 * @return dummy comment
	 */
	private boolean isJarPackaging(File pomFile) {
		try {
			Model model = PomXmlUtils.unmarshallPomFile(pomFile);

			// packaging "jar" is the default and may not be defined
			if (model.getPackaging() == null || model.getPackaging().equals("") || model.getPackaging().equals("jar")) {
				return true;
			}
		} catch (JAXBException e) {
			getLog().debug("Exception unmarshalling ('" + pomFile.getAbsolutePath() + "')", e);
		}

		// this should really never happen
		return false;
	}

	/**
	 * goal of the method is to create sharedLibs projects defined by Maven dependencies 
	 * for this purpose the method performs the following tasks: 
	 * - unpacks dependencies of scope "compile" and type "zip" to unpackDependencyDirectory  
	 * - filter dependencies for bar files - and unpack them to  unpackBarDirectory
	 * - filter unpacked bar files for sharedLibs and unpack them to the workspace directory  
	 * - create a .project file for the sharedLibs projects 
	 * 
	 * @throws MojoExecutionException If an exception occurs
	 */
	private void unpackaceDependencies() throws MojoExecutionException {

		// define the directory to be unpacked into and create it
		workspace.mkdirs();

		// unpack all dependencies that match the given scope to the
		// "unpackDependencyDirectory
		executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("2.8")),
				goal("unpack-dependencies"),
				configuration(element(name("outputDirectory"), unpackDependenciesDirectory.getAbsolutePath()),
						element(name("includeTypes"), UNPACK_ace_DEPENDENCY_TYPES),
						element(name("includeScope"), UNPACK_ace_DEPENDENCY_SCOPE)),
				executionEnvironment(project, session, buildPluginManager));

		try {
			// step 1: unpack all bar files
			List<File> barFiles = FileUtils.getFiles(unpackDependenciesDirectory, "*.bar", "default.bar");
			for (File barFile : barFiles) {
				new ZipFile(barFile).extractAll(unpackBarDirectory.getAbsolutePath());
				getLog().info(
						"unpacking " + barFile.getName() + " to " + unpackBarDirectory.getAbsolutePath().toString());
			}

			// step 2: unpack all sharedlibs - and unpack them directly to the workspace
			List<File> sharedLibs = FileUtils.getFiles(unpackBarDirectory, "*.shlibzip", "default.shlibzip");
			for (File sharedLib : sharedLibs) {
				String projectName = FileUtils.removeExtension(sharedLib.getName());

				// determine the targetDirectory
				String targetDirectory = workspace.getAbsolutePath().toString() + "/" + projectName;
				File projectDirectory = new File(targetDirectory);

				new ZipFile(sharedLib).extractAll(projectDirectory.getAbsolutePath());
				getLog().info(
						"unpacking " + sharedLib.getName() + " to " + projectDirectory.getAbsolutePath().toString());

				ClassLoader classloader = Thread.currentThread().getContextClassLoader();
				InputStream inStream = classloader.getResourceAsStream("templates/project.txt");

				String projectFile = IOUtils.toString(inStream);
				projectFile = projectFile.replace("projectname", projectName);

				String targetFileName = workspace.getAbsolutePath().toString() + "/" + projectName + "/.project";
				File targetFile = new File(targetFileName);

				Files.write(Paths.get(targetFile.getAbsolutePath()), projectFile.getBytes(StandardCharsets.UTF_8));

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	/**
	 * @return the types that will be unpacked when preparing the Bar Build
	 *         Workspace
	 */
	public static Set<String> getUnpackaceDependencyTypes() {
		HashSet<String> types = new HashSet<String>();
		for (String type : DependencyUtil.tokenizer(UNPACK_ace_DEPENDENCY_TYPES)) {
			types.add(type);
		}
		return types;
	}
}
