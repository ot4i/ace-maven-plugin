package ibm.maven.plugins.ace.mojos;

import ibm.maven.plugins.ace.utils.EclipseProjectUtils;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Validates the ace project
 */
@Mojo(name = "validate-project")
public class ValidateProjectMojo extends AbstractMojo {


    /**
     * The path of the workspace in which the projects were created.
     */
    @Parameter(property = "ace.workspace", defaultValue = "${project.build.directory}/ace/workspace", required = true)
    protected File workspace;

    /**
     * The Maven Project Object
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    public void execute() throws MojoFailureException {

        File projectDirectory = project.getBasedir();
        String projectDirectoryName = projectDirectory.getName();

        // checks that the directory name is the same as the name in the .project file
        String eclipseProjectName = EclipseProjectUtils.getProjectName(projectDirectory);
        if (!projectDirectoryName.equals(eclipseProjectName)) {
            throw new MojoFailureException("The Project Directory Name ('" + projectDirectoryName + "') is not the same as the Project Name (in .project file) ('" + eclipseProjectName + "')");
        }

        // checks that the directory name is the same as the artifactId from the pom.xml file
        String artifactId = project.getArtifactId();
        if (!projectDirectoryName.equals(artifactId)) {
            throw new MojoFailureException("Project Directory Name ('" + projectDirectoryName + "') is not the same as the Maven artifactId (in pom.xml) ('" + project.getName() + "')");
        }


    }

}
