package ibm.maven.plugins.ace.mojos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
/* and on for maven resolver */
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;

import com.sun.tools.internal.ws.wsdl.document.jaxws.Exception;

import ibm.maven.plugins.ace.utils.CommandExecutionUtil;
import ibm.maven.plugins.ace.utils.EclipseProjectUtils;
import ibm.maven.plugins.ace.utils.MavenUtils;
import ibm.maven.plugins.ace.utils.ZipUtils;

/**
 * Creates a .bar file from a ace-bar Project.
 * 
 * Implemented with help from:
 * https://github.com/TimMoore/mojo-executor/blob/master/README.md
 */

@Mojo(name = "create-bar", defaultPhase = LifecyclePhase.COMPILE)
public class CreateBarMojo extends AbstractMojo {

	/**
	 * a comma separated list of dependency types to be unpacked
	 */

	/**
	 * The name of the BAR (compressed file format) archive file where the result is
	 * stored.
	 */
	@Parameter(property = "ace.barName", defaultValue = "${project.build.directory}/ace/${project.artifactId}-${project.version}.bar", required = true)
	protected File barName;

	/**
	 * Whether the applybaroverride command should be executed or not
	 */
	@Parameter(property = "ace.applybaroverride", defaultValue = "true", required = true)
	protected Boolean applyBarOverride;

	/**
	 * Refreshes the projects in the workspace and then invokes a clean build before
	 * new items are added to the BAR file.
	 */
	@Parameter(property = "ace.cleanBuild", defaultValue = "true", required = true)
	protected boolean cleanBuild;

	/**
	 * The name of the trace file to use when creating bar files
	 */
	@Parameter(property = "ace.createBarTraceFile", defaultValue = "${project.build.directory}/bar-compile-trace.txt", required = true)
	protected File createBarTraceFile;

	/**
	 * Include "-deployAsSource" parameter?
	 */
	@Parameter(property = "ace.deployAsSource", defaultValue = "true", required = true)
	protected boolean deployAsSource;

	/**
	 * Compile ESQL for brokers at Version 2.1 of the product.
	 */
	@Parameter(property = "ace.esql21", defaultValue = "false", required = true)
	protected boolean esql21;

	/**
	 * Exclude artifacts pattern (or patterns, comma separated). By default, exclude
	 * pom.xml's as each project will have one and this causes a packaging error.
	 */
	@Parameter(property = "ace.excludeArtifactsPattern", defaultValue = "**/pom.xml")
	protected String excludeArtifactsPattern;

	/**
	 * Include artifacts pattern (or patterns, comma separated). By default, the
	 * default value used for mqsipackagebar, except .esql & .subflow, which as not
	 * compilable
	 * 
	 * @see <a href=
	 *      "http://www-01.ibm.com/support/knowledgecenter/SSMKHH_9.0.0/com.ibm.etools.mft.doc/bc31720_.htm">ace9
	 *      Documentation</a>
	 */
	@Parameter(property = "ace.includeArtifactsPattern", defaultValue = "**/*.xsdzip,**/*.tblxmi,**/*.xsd,**/*.wsdl,**/*.dictionary,**/*.xsl,**/*.xslt,**/*.xml,**/*.jar,**/*.inadapter,**/*.outadapter,**/*.insca,**/*.outsca,**/*.descriptor,**/*.idl,**/*.map,**/*.msgflow", required = true)
	protected String includeArtifactsPattern;

	/**
	 * Projects containing files to include in the BAR file in the workspace.
	 * Required for a new workspace. A new workspace is a system folder which don't
	 * contain a .metadata folder.
	 */
	@Parameter(property = "ace.projectName", defaultValue = "")
	protected String projectName;

	/*
	 * Application/Service Name to add to bar file
	 */

	@Parameter(property = "ace.applicationName", defaultValue = "${project.artifactId}")
	protected String applicationName;

	/**
	 * Whether classloaders are in use with this bar
	 */
	@Parameter(property = "ace.skipWSErrorCheck", defaultValue = "false")
	protected Boolean skipWSErrorCheck;

	/**
	 * Name of the directory to create the tmp files; required to build the project
	 */
	@Parameter(property = "ace.fileTmpDir", defaultValue = "${project.build.directory}", required = true)
	protected String fileTmpDir;

	/**
	 * Installation directory of the ace Toolkit
	 */
	@Parameter(property = "ace.toolkitInstallDir", required = true)
	protected File toolkitInstallDir;

	/**
	 * Installation directory of the ace Toolkit
	 */
	@Parameter(property = "ace.aceRunDir", required = true)
	protected File aceRunDir;

	/**
	 * Major Version number of the ace Toolkit. (Current not used, but will be
	 * needed when support for difference Versions with different options is
	 * supported)
	 */
	@Parameter(property = "ace.toolkitVersion", defaultValue = "10")
	protected String toolkitVersion;

	/**
	 * Appends the _ (underscore) character and the value of VersionString to the
	 * names of the compiled versions of the message flows (.cmf) files added to the
	 * BAR file, before the file extension.
	 */
	@Parameter(property = "ace.versionString", defaultValue = "${project.version}")
	protected String versionString;

	/**
	 * The path of the workspace in which the projects are extracted to be built.
	 */
	@Parameter(property = "ace.workspace", defaultValue = "${project.basedir}/..", required = true)
	protected File workspace;

	/**
	 * Pattern (or patterns, comma separated) of jars to be excluded from the
	 * generated bar file
	 */
	@Parameter(property = "ace.discardJarsPattern", defaultValue = "**/javacompute_**.jar,**/jplugin2_**.jar")
	protected String discardJarsPattern;

	/**
	 * Whether classloaders are in use with this bar
	 */
	@Parameter(property = "ace.useClassloaders", defaultValue = "false", required = true)
	protected Boolean useClassloaders;

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
	 * Whether additional debug information should be printed out
	 */
	@Parameter(property = "ace.debug", defaultValue = "false", required = true, readonly = true)
	protected Boolean debug;

	/**
	 * ibmint parameter section; added by C.Weiss, IBM 02/2022
	 **/

	/**
	 * Whether ibmint package should be used instead of mqsicreatebar
	 */
	@Parameter(property = "ace.ibmint", defaultValue = "false", required = false)
	protected Boolean ibmint;

	/**
	 * filepath for overrides-file; set if overrides-file parameter should be used
	 * for ibmint package
	 */
	@Parameter(property = "ace.overridesFile", defaultValue = "", required = false)
	protected String overridesFile;

	/**
	 * filepath for keywords-file; set if keywords-file parameter should be used for
	 * ibmint package
	 */
	@Parameter(property = "ace.keywordsFile", defaultValue = "", required = false)
	protected String keywordsFile;

	/**
	 * Wheter java code should be NOT compiled
	 */
	@Parameter(property = "ace.doNotCompileJava", defaultValue = "false", required = false)
	protected Boolean doNotCompileJava;

	/**
	 * Wheter maps and schemas should be compiled
	 */
	@Parameter(property = "ace.compileMapsAndSchemas", defaultValue = "false", required = false)
	protected Boolean compileMapsAndSchemas;

	/**
	 * added temporary mqsiWorkDir - only used in context of ibmint
	 */
	@Parameter(property = "ace.mqsiTempWorkDir", defaultValue = "${project.build.directory}/tmp-work-dir", required = true, readonly = true)
	protected File mqsiTempWorkDir;

	/**
	 * The current repository/network configuration of Maven.
	 */
	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;

	/**
	 * The Maven PluginManager Object
	 */
	@Component
	protected BuildPluginManager buildPluginManager;

	/**
	 * The project's remote repositories to use for the resolution.
	 */
	@Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
	private List<RemoteRepository> remoteRepos;

	/**
	 * The entry point to Maven Artifact Resolver, i.e. the component doing all the
	 * work.
	 */
	@Component
	private RepositorySystem repoSystem;

	/**
	 * Flag for using of ibmint add java project resources java project
	 * /src/main/resources
	 */
	@Parameter(property = "ace.ibmintResources", defaultValue = "false", required = false)
	protected Boolean ibmintResources;

	private List<String> addObjectsAppsLibs() throws MojoFailureException {
		List<String> params = new ArrayList<String>();
		List<String> apps = new ArrayList<String>();
		List<String> libs = new ArrayList<String>();
		List<String> policies = new ArrayList<String>();

		// validate project nature (type)

		if (EclipseProjectUtils.isApplication(new File(workspace, applicationName), getLog())) {
			apps.add(applicationName);
		} else if (EclipseProjectUtils.isLibrary(new File(workspace, applicationName), getLog())) {
			libs.add(applicationName);
		} else if (EclipseProjectUtils.isPolicyProject(new File(workspace, applicationName), getLog())) {
			policies.add(applicationName);
		}

		for (org.apache.maven.model.Dependency dependency : project.getDependencies()) {

			// only check for dependencies with scope "compile" AND no standard jar files
			if ((!dependency.getScope().equals("compile")) || (dependency.getType().equals("jar"))) {
				continue;
			}

			// the projectName is the directoryName is the artifactId
			projectName = dependency.getArtifactId();

			// Updated to exclude Shared library
			if (EclipseProjectUtils.isApplication(new File(workspace, projectName), getLog())) {
				apps.add(projectName);
			} else if (EclipseProjectUtils.isLibrary(new File(workspace, projectName), getLog())) {
				if (!EclipseProjectUtils.isSharedLibrary(new File(workspace, projectName), getLog())) {
					libs.add(projectName);
				} else if (EclipseProjectUtils.isPolicyProject(new File(workspace, projectName), getLog())) {
					policies.add(projectName);
				}
			}
		}

		/* logic to create mqsibar command */

		// logic for mqsicreatebar
		if (!apps.isEmpty()) {
			params.add("-a");
			params.addAll(apps);
		}

		// if there are libraries, add them
		if (!libs.isEmpty()) {
			params.add("-l");
			params.addAll(libs);
		}

		// if there are policy projects, add them
		if (!policies.isEmpty()) {
			params.add("-x");
			params.addAll(policies);
			params.add("-p");
			params.addAll(policies);
		}

		return params;
	}

	/*
	 * constructs the parameter either for mqsicreatebar or ibmint package
	 */
	protected List<String> constructParams() throws MojoFailureException {
		List<String> params = new ArrayList<String>();

		// create the workspace
		createWorkspaceDirectory();

		// mqsicreate bar parameter
		params.add("-data");
		params.add(workspace.toString());

		params.add("-b");
		params.add(barName.getAbsolutePath());

		// cleanBuild - optional
		if (cleanBuild) {
			params.add("-cleanBuild");
		}

		// use esql21 - optional
		if (esql21) {
			params.add("-esql21");
		}

		// deployAsSource? - optional
		if (deployAsSource) {
			params.add("-deployAsSource");
		}

		params.addAll(addObjectsAppsLibs());

		// skipWSErrorCheck - option
		if (skipWSErrorCheck) {
			params.add("-skipWSErrorCheck");
		}
		// always trace into the file target/ace/mqsicreatebartrace.txt
		params.add("-trace");
		params.add("-v");
		params.add(createBarTraceFile.getAbsolutePath());

		return params;
	}

	/**
	 * @throws MojoFailureException If an exception occurs
	 */
	protected void createWorkspaceDirectory() throws MojoFailureException {
		if (!workspace.exists()) {
			workspace.mkdirs();
		}
		if (!workspace.isDirectory()) {
			throw new MojoFailureException("Workspace parameter is not a directory: " + workspace.toString());
		}
	}

	public void execute() throws MojoFailureException, MojoExecutionException {

		getLog().info("Creating bar file: " + barName);

		File barDir = barName.getParentFile();
		if (!barDir.exists()) {
			barDir.getParentFile().mkdirs();
		}

		if (ibmint) {

			/* handling for ibmint - outsourced to dedicated method */
			ibmintCompile();
		} else {

			// * handling for ibmint - outsourced to dedicated method */
			List<String> params = constructParams();
			executeMqsiCreateBar(params);

			try {
				/* check for classloaders */
				if (useClassloaders) {
					getLog().info("Classloaders in use. All jars will be removed from the bar file.");
					ZipUtils.removeFiles(barName, "**/*.jar");
				} else {
					// remove the jars specified with discardJarsPattern
					if (discardJarsPattern != null && !"".equals(discardJarsPattern)) {
						getLog().info(
								"Classloaders are not in use. The following jars will be removed from the bar file: "
										+ discardJarsPattern);
						ZipUtils.removeFiles(barName, discardJarsPattern);
					}
				}

			} catch (IOException e) {
				throw new MojoFailureException("Error removing jar files from bar file", e);
			}

		}

	}

	public void ibmintCompile() throws MojoFailureException {

		List<String> params = new ArrayList<String>();
		List<String> commands = new ArrayList<String>();

		// create the workspace
		createWorkspaceDirectory();

		// set System specific settings
		String osName = System.getProperty("os.name").toLowerCase();
		String exportCommand = new String();
		String pathDelimiter = new String();

		StringBuffer ibmintCommand = new StringBuffer("");

		/* step 1: set system operating specific parameter */

		if (osName.contains("windows")) {
			exportCommand = "SET";
			pathDelimiter = ";";

		} else if (osName.contains("linux") || osName.contains("mac os x")) {
			exportCommand = "export";
			pathDelimiter = ":";
		} else {
			throw new MojoFailureException("Unexpected OS: " + osName);
		}

		/* step 2: get all java used projects */
		List<String> javaProjects = EclipseProjectUtils
				.getJavaProjectsDependencies(new File(workspace, applicationName), workspace, getLog());

		/*
		 * step 3: get all maven dependencies from the java project
		 * 
		 * artifactCoordinate (as String)
		 * <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}
		 * 
		 */
		List<Artifact> dependencies = MavenUtils.getDependencies(workspace, javaProjects, "compile", getLog());

		/* step 4: resolve (and thus download) all dependencies */
		List<File> files = MavenUtils.resolveArtifacts(dependencies, remoteRepos, repoSystem, repoSession, getLog());

		/* copy jars to the project */
		int count = 0;
		StringBuffer classpathExt = new StringBuffer("");
		for (File dependencyFile : files) {
			getLog().debug("handling downloaded depenendency: " + dependencyFile.getName());
			String targetFileName = new String(
					workspace.toString() + "/" + applicationName + "/" + dependencyFile.getName());
			Path targetPath = Paths.get(targetFileName);
			try {
				Files.copy(Paths.get(dependencyFile.getAbsolutePath()), targetPath,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new MojoFailureException(e.getCause());
			}

			if (count > 0) {
				getLog().debug("adding seperator for classpath");
				classpathExt.append(pathDelimiter);
			}
			classpathExt.append(targetPath.toString());
			count++;
		}

		// assembling params
		ibmintCommand.append("ibmint package ");
		ibmintCommand.append("--input-path " + "\"" + workspace.toString() + "\" ");
		ibmintCommand.append("--output-bar-file \"" + barName.getAbsolutePath() + "\" ");

		if ((overridesFile != null) && (overridesFile.length() > 0)) {
			ibmintCommand.append("--overrides-file " + overridesFile + " ");
		}

		if ((keywordsFile != null) && (keywordsFile.length() > 0)) {
			ibmintCommand.append("--keywords-file " + keywordsFile + " ");
		}

		if (doNotCompileJava) {
			ibmintCommand.append("--do-not-compile-java ");
		}

		if (compileMapsAndSchemas) {
			ibmintCommand.append("--compile-maps-and-schemas ");
		}

		// adding project
		ibmintCommand.append("--project " + applicationName + " ");

		for (String javaProject : javaProjects) {
			ibmintCommand.append("--project " + javaProject + " ");
		}

		// adding trace
		ibmintCommand.append("--trace " + "\"" + createBarTraceFile.getAbsolutePath() + "\" ");

		// building up required command list
		commands.add(exportCommand + " MQSI_REGISTRY=\"" + mqsiTempWorkDir + "/config\"");
		commands.add("mqsicreateworkdir \"" + mqsiTempWorkDir + "\"");
		commands.add(exportCommand + " MQSI_WORKPATH=\"" + mqsiTempWorkDir + "/config\"");

		// handle MQSI_EXTRA_BUILD_CLASSPATH

		if ((classpathExt != null) && (classpathExt.length() > 0)) {
			commands.add(exportCommand + " MQSI_EXTRA_BUILD_CLASSPATH=" + classpathExt + " ");
		}

		commands.add(ibmintCommand.toString());

		if (debug) {
			Map<String, String> env = System.getenv();
			getLog().info("**** start debug environment");
			env.forEach((k, v) -> getLog().info(k + ":" + v));
			getLog().info("**** end debug environment");
		}
		CommandExecutionUtil.runCommand(aceRunDir, fileTmpDir, commands, getLog());

		// Fix attach resource from java projects
		getLog().info("evaluate ibmintResources: "+ibmintResources);
		if (ibmintResources) {
			attachResourcesInJavaProjects(barName, javaProjects, workspace, applicationName);
		}
	}

	/**
	 * runs either ibmint package or mqsicreatebar - depending on the plugin config
	 * 
	 * @param params
	 * @throws MojoFailureException If an exception occurs
	 */
	private void executeMqsiCreateBar(List<String> params) throws MojoFailureException {

		getLog().info("running mqsicreatebar");

		if (debug) {
			Map<String, String> env = System.getenv();
			getLog().info("**** start debug environment");
			env.forEach((k, v) -> getLog().info(k + ":" + v));
			getLog().info("**** end debug environment");
		}

		String cmd = new String("mqsicreatebar");
		CommandExecutionUtil.runCommand(aceRunDir, fileTmpDir, cmd, params, getLog());

	}

	/**
	 * Fix for attach Resources in java Projects
	 */
	private void attachResourcesInJavaProjects(File _barName, List<String> _javaProjects, File workspace, String applicationName) throws MojoFailureException {
		String sep = System.getProperty("file.separator");
		String tmpDir = mqsiTempWorkDir + sep + UUID.randomUUID();
		String javaResource = "";
		getLog().info("adding java resource files");
		
		//determine file extension 
		String fileExtension  = new String(""); 
		
		if (EclipseProjectUtils.isApplication(new File (workspace,applicationName), getLog())) {
			fileExtension="appzip"; 
		} else if (EclipseProjectUtils.isSharedLibrary(new File (workspace,applicationName), getLog())) {
			fileExtension="shlibzip";
		} else if (EclipseProjectUtils.isLibrary(new File (workspace,applicationName), getLog())) {
			fileExtension="libzip"; 
		} else { 
			throw  new MojoFailureException("unsupported ACE application project type"); 
		}
		
		try {
			Files.createDirectories(Paths.get(tmpDir));
			// extract bar to /temp/bar
			ZipUtils.unpack(_barName, tmpDir + sep + "bar");
			// extract /temp/bar/applicationName.type (e.g.shlibzip) to /tmp/type
			ZipUtils.unpack(new File(tmpDir + sep + "bar" + sep + applicationName + "."+fileExtension),
					tmpDir + sep + fileExtension);
			// extract java projects to /tmp/jar/javaproject
			for (String javaProject : _javaProjects) {
				// get path to workspace of java projects
				javaResource = workspace + sep + javaProject + sep + "src" + sep + "main" + sep + "resources";
				// Patch only java projects with resources
				if (new File(javaResource).isDirectory()) {
					ZipUtils.unpack(new File(tmpDir + sep + fileExtension + sep + javaProject + ".jar"),
							tmpDir + sep + "jar" + sep + javaProject);
					// get path to workspace of java projects
					javaResource = workspace + sep + javaProject + sep + "src" + sep + "main" + sep + "resources";
					getLog().info("Copy java resources:" + javaResource);
					getLog().info("Copy java resources to:" + tmpDir + sep + "jar" + sep + javaProject);
					// add /src/main/resources
					FileUtils.copyDirectoryStructure(new File(javaResource), new File(tmpDir + sep + "jar" + sep + javaProject));
				}
			}
			// pack java projects to /tmp/jar
			for (String javaProject : _javaProjects) {
				// get path to workspace of java projects
				javaResource = workspace + sep + javaProject + sep + "src" + sep + "main" + sep + "resources";
				// Pack only projects with resources
				if (new File(javaResource).isDirectory()) {
					ZipUtils.pack(tmpDir + sep + "jar" + sep + javaProject,
							tmpDir + sep + "jar" + sep + javaProject + ".jar");
					getLog().info("Jar created:" + tmpDir + sep + "jar" + sep + javaProject + ".jar");
					// copy (delete old and move new one)
					new File(tmpDir + sep + fileExtension + sep + javaProject + ".jar").delete();
					Files.move(Paths.get(tmpDir + sep + "jar" + sep + javaProject + ".jar"),
							Paths.get(tmpDir + sep + fileExtension + sep + javaProject + ".jar"),
							StandardCopyOption.REPLACE_EXISTING);
				}
			}
			// pack 'zip' to /tmp
			ZipUtils.pack(tmpDir + sep + fileExtension, tmpDir + sep + applicationName + "."+fileExtension);
			getLog().info("new file created:" + tmpDir + sep + applicationName + "."+fileExtension);
			// copy
			new File(tmpDir + sep + "bar" + sep + applicationName + "."+fileExtension).delete();
			Files.move(Paths.get(tmpDir + sep + applicationName + "."+fileExtension),
					Paths.get(tmpDir + sep + "bar" + sep + applicationName + "."+fileExtension),
					StandardCopyOption.REPLACE_EXISTING);
			// pack bar to /tmp
			ZipUtils.pack(tmpDir + sep + "bar", tmpDir + sep + _barName.getName());
			getLog().info("Bar created:" + tmpDir + sep + _barName.getName());
			// copy and cleanup
			Files.move(Paths.get(tmpDir + sep + _barName.getName()),Paths.get(_barName.getPath()),StandardCopyOption.REPLACE_EXISTING);
			// Files.delete(Paths.get(tmpDir));
			getLog().info("Bar overwritten:" + _barName.getPath());
		} catch (IOException ioe) {
			throw new MojoFailureException("failure updating file:" + ioe.getMessage());
		}
	}

}
