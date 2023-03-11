package ibm.maven.plugins.ace.mojos;

import java.io.File;

import javax.xml.bind.JAXBException;

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

import ibm.maven.plugins.ace.generated.maven_pom.Model;
import ibm.maven.plugins.ace.utils.PomXmlUtils;

/**
 * Unpacks the dependent App Connect Enterprise Projects.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * requiresDependencyResolution below is required for the unpack-dependencies goal to work correctly. See https://github.com/TimMoore/mojo-executor/issues/3
 */
@Mojo(name = "validate-bar-build-workspace", requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateBarBuildWorkspaceMojo extends AbstractMojo {

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
     * The path of the workspace in which the projects will be unpacked.
     */
    @Parameter(property = "ace.unpackDependenciesDirectory", defaultValue = "${project.build.directory}/ace/dependencies", required = true, readonly = true)
    protected File unpackDependenciesDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {

        // Comment Christoph Weiss, IBM 14.03.22: below commented block is from a previous code committer 
    	
    	// the following code would be a nicer implementation, but breaks the Integration Tests
        //
        // Set<String> unpackaceDependencyTypes = PrepareBarBuildWorkspaceMojo.getUnpackaceDependencyTypes();
        //
        // // loop through the dependencies
        // for (Dependency dependency : project.getDependencies()) {
        //
        // // ignore dependencies that won't be unpacked (ie. non-ace dependencies)
        // if (!unpackaceDependencyTypes.contains(dependency.getType())) {
        // continue;
        // }
        //
        // String projectDirectoryName = dependency.getArtifactId();

    	/*Comment Christoph Weiss, IBM 14.03.22: this is the original code -what does not make direkt sense 
    	/* additional thougts: it would only make sense to check that all dependent projects are in place before we proceed. 
    	 * this would be all projects defined in the .project file of the main application 
    	 */
    	/* 
        // loop through the project directories
        File[] projects = workspace.listFiles();
        //
        for (File projectDirectory : projects) {
            // ignore the directory .metadata
            // normally it won't exist unless a previous build failed during bar packaging and no clean was specified on the new build
           //Added the code to ignore folders starting with dot as those are hidden folders and not to be used when compiling the project
        	// Also ignore other text files
        	if (".metadata".equals(projectDirectory.getName()) 
        			||projectDirectory.getName().startsWith(".")
        			||projectDirectory.isFile()
                    ||projectDirectory.getName().endsWith("@tmp") ) {
                continue;
            }

            String projectDirectoryName = projectDirectory.getName();
            

            // checks that the directory name is the same as the name in the .project file
            String eclipseProjectName = EclipseProjectUtils.getProjectName(projectDirectory);
            if (!projectDirectoryName.equals(eclipseProjectName)) {
                // throw new MojoFailureException("The Project Directory Name ('" + projectDirectoryName + "') is not the same as the Project Name (in .project file) ('" + eclipseProjectName + "')");
            	getLog().debug("The Project Directory Name ('" + projectDirectoryName + "') is not the same as the Project Name (in .project file) ('" + eclipseProjectName + "')");
            }

            // checks that the directory name is the same as the artifactId from the pom.xml file
            String artifactId = getProjectArtifactId(projectDirectory);
            if (artifactId != null && !projectDirectoryName.equals(artifactId)) {
                // throw new MojoFailureException("The Project Directory Name ('" + projectDirectoryName + "') is not the same as the Maven artifactId (in pom.xml): " + artifactId);
            	getLog().debug("The Project Directory Name ('" + projectDirectoryName + "') is not the same as the Maven artifactId (in pom.xml): " + artifactId);
            }

        }
        */ 
    }

    /**
     * return the artifactId (if the pom.xml exists), otherwise null
     * 
     * @param projectDirectory directory containing pom.xml
     * @return the artifactId from the pom.xml
     * @throws MojoFailureException if something goes wrong
     */
    private String getProjectArtifactId(File projectDirectory) throws MojoFailureException {
        File pomFile = new File(projectDirectory, "pom.xml");
        String artifactId = null;
        try {
            Model model = PomXmlUtils.unmarshallPomFile(pomFile);
            artifactId = model.getArtifactId();
        } catch (JAXBException e) {
            getLog().debug("Exception unmarshalling ('" + pomFile.getAbsolutePath() + "')", e);
        }
        return artifactId;
    }

}
