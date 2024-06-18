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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Creates a .bar file from a ace-bar Project.
 * 
 * Implemented with help from:
 * https://github.com/TimMoore/mojo-executor/blob/master/README.md
 */
@Mojo(name = "package-ace-bar")
public class PackageaceBarMojo extends CreateBarMojo {

	/**
	 * The path to write the assemblies/ace-bar-project.xml file to before invoking
	 * the maven-assembly-plugin.
	 */
	@Parameter(defaultValue = "${project.build.directory}/assemblies/ace-bar-project.xml", readonly = true)
	private File buildAssemblyFile;

	/**
	 * The name of the BAR (compressed file format) archive file where the result is
	 * stored.
	 */
	@Parameter(property = "ace.barName", defaultValue = "${project.build.directory}/ace/${project.artifactId}-${project.version}.bar", required = true)
	protected File barName;

	/**
	 * The path to store the "source code"
	 */
	@Parameter(defaultValue = "${project.build.directory}/ace", readonly = true)
	private String outputDirectorySourceCode;

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
	 * Whether the applybaroverride command should be executed or not
	 */
	@Parameter(property = "ace.packageSource", defaultValue = "true", required = true)
	protected Boolean packageSource;

	/**
	 * The Maven PluginManager Object
	 */
	@Component
	protected BuildPluginManager buildPluginManager;

	@Override
	public void execute() throws MojoFailureException, MojoExecutionException {

		packageaceBarArtifact();

	}

	private void packageaceBarArtifact() throws MojoFailureException, MojoExecutionException {

		/*
		 * add source code to ace directory
		 */
		if (packageSource) {
			getLog().info("adding source code to assembly");

			executeMojo(
					plugin(groupId("org.apache.maven.plugins"), artifactId("maven-source-plugin"), version("3.2.1")),
					goal("jar-no-fork"),
					configuration(element(name("outputDirectory"), outputDirectorySourceCode),
							element(name("excludes"), "target/**"),
							element(name("attach"),"false")
							),
					executionEnvironment(project, session, buildPluginManager));
		} else {
			getLog().info("no source code packaged for the assembly");
		}

		/* attach bar to deploy */
		
		try {
			
			getLog().info("try to attach bar file");
			if (barName.exists()) {

				getLog().info("found bar file: " + barName.getAbsolutePath());
				executeMojo(
						plugin(groupId("org.codehaus.mojo"), artifactId("build-helper-maven-plugin"), version("3.3.0")),
						goal("attach-artifact"),
						configuration(element("artifacts",
								element("artifact", element("file", barName.getAbsolutePath()), element("type", "bar")
										))),
						executionEnvironment(project, session, buildPluginManager));

			}

		} catch (Exception e) {
			throw new MojoFailureException("Error attaching the bar file via the build-helper-maven-plugin: " + e.getMessage());
		}

		/*
		 * package based on assembly defintion
		 */
		InputStream is = this.getClass().getResourceAsStream("/assemblies/ace-bar-project.xml");
		FileOutputStream fos;
		buildAssemblyFile.getParentFile().mkdirs();
		try {
			fos = new FileOutputStream(buildAssemblyFile);
		} catch (FileNotFoundException e) {
			// should never happen, as the file is packaged in this plugin's jar
			throw new MojoFailureException("Error creating the build assembly file: " + buildAssemblyFile);
		}
		try {
			IOUtil.copy(is, fos);
		} catch (IOException e) {
			// should never happen
			throw new MojoFailureException("Error creating the assembly file: " + buildAssemblyFile.getAbsolutePath());
		}

		// mvn org.apache.maven.plugins:maven-assembly-plugin:2.4:single
		// -Ddescriptor=target\assemblies\ace-bar-project.xml
		// -Dassembly.appendAssemblyId=false

		executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-assembly-plugin"), version("2.4")),
				goal("single"),
				configuration(element(name("descriptor"), "${project.build.directory}/assemblies/ace-bar-project.xml"),
						element(name("appendAssemblyId"), "false")),
				executionEnvironment(project, session, buildPluginManager));

		// delete the archive-tmp directory
		try {
			FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "archive-tmp"));
		} catch (IOException e) {
			throw new MojoFailureException("Error deleting the archive-tmp directory: "+e.getMessage());
		}
	}

}
