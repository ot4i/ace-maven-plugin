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

import ibm.maven.plugins.ace.utils.CommandExecutionUtil;
import ibm.maven.plugins.ace.utils.ConfigurablePropertiesUtil;
import ibm.maven.plugins.ace.utils.EclipseProjectUtils;
import ibm.maven.plugins.ace.utils.ProcessOutputCatcher;
import ibm.maven.plugins.ace.utils.ProcessOutputLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Goal which reads the a bar file, including creating a list of configurable properties
 */
@Mojo(name = "validate-configurable-properties", defaultPhase = LifecyclePhase.PACKAGE)
public class ValidateConfigurablePropertiesMojo extends AbstractMojo {

    /**
     * Whether the applybaroverride command should be executed or not
     */
    @Parameter(property = "ace.applybaroverride", defaultValue = "true", required = true)
    protected Boolean applyBarOverride;

    /**
     * Whether the applybaroverride command should be executed or not
     */
    @Parameter(property = "ace.applyBarOverrideRecursively", defaultValue = "true", required = true)
    protected Boolean applyBarOverrideRecursively;

    /**
     * The name of the BAR (compressed file format) archive file where the result is stored.
     * 
     */
    @Parameter(property = "ace.barName", defaultValue = "${project.build.directory}/ace/${project.artifactId}-${project.version}.bar", required = true)
    protected File barName;

    /**
     * The name of the default properties file to be generated from the bar file.
     * 
     */
    @Parameter(property = "ace.configurablePropertiesFile", defaultValue = "${project.build.directory}/ace/default.properties", required = true)
    protected File defaultPropertiesFile;
    
    /**
     * The path of directory containing properties file for override.
     * 
     */
    @Parameter(property = "ace.configPropFileDirectory", defaultValue = "${project.build.directory}/ace", required = true)
    protected File configPropFileDirectory;

    /**
     * Whether or not to fail the build if properties are found to be invalid.
     */
    @Parameter(property = "ace.failOnInvalidProperties", defaultValue = "true", required = true)
    protected Boolean failOnInvalidProperties;
    
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
     * Installation directory of the ace runtime
     */
    @Parameter(property = "ace.aceRunDir", required = true)
    protected File aceRunDir;
    
    /**
     * Installation directory of the ace10 Toolkit
     */
  /*  @Parameter(property = "ace.ace10toolkit", required = true)
    protected File ace10toolkit;
    */
    
    /**
     * The name of the trace file to use when reading bar files
     */
 /*   @Parameter(property = "ace.readBarTraceFile", defaultValue = "${project.build.directory}/readbartrace.txt", required = true)
    protected File readBarTraceFile;
*/
    /**
     * Appends the _ (underscore) character and the value of VersionString to the names of the compiled versions of the message flows (.cmf) files added to the BAR file, before the file extension.
     */
    @Parameter(property = "ace.versionString", defaultValue = "${project.version}")
    protected String versionString;

    /**
     * The path of the workspace in which the projects are extracted to be built.
     */
    @Parameter(property = "ace.workspace", defaultValue = "${project.build.directory}/ace/workspace", required = true)
    protected File workspace;


    /**
     * The basename of the trace file to use when applybaroverriding bar files
     */
    @Parameter(property = "ace.applyBarOverrideTraceFile", defaultValue = "${project.build.directory}/applybaroverridetrace.txt", required = true)
    protected File applyBarOverrideTraceFile;

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
     * Projects containing files to include in the BAR file in the workspace. Required for a new workspace. A new workspace is a system folder which don't contain a .metadata folder.
     */
    @Parameter(property = "ace.applicationName")
    protected String applicationName;


    public void execute() throws MojoFailureException, MojoExecutionException {

    	
    	copyAndFilterResources();

        getLog().info("Reading bar file: " + barName);

        List<String> params = new ArrayList<String>();
        params.add("-b");
        params.add(barName.getAbsolutePath());

        // process the bar file recursively (applies to applications and libraries)
        params.add("-r");
        
      /*  params.add("-v");
        params.add(readBarTraceFile.getAbsolutePath());
*/

        List<String> output = executeReadBar(params);

        List<String> configurableProperties = getConfigurableProperties(output);

        writeToFile(configurableProperties, defaultPropertiesFile);

        if (applyBarOverride){
        validatePropertiesFiles(ConfigurablePropertiesUtil.getPropNames(configurableProperties));

        
        executeApplyBarOverrides();
    	}
    }

    private void copyAndFilterResources() throws MojoFailureException, MojoExecutionException {

        getLog().debug("Project Build Resources: " + project.getBuild().getResources().toString());

        // copy the main resources
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-resources-plugin"), version("2.6")), goal("copy-resources"), configuration(element(name("outputDirectory"),
                "${project.build.directory}/ace"), element(name("resources"), element(name("resource"),
                // TODO hard-coding this isn't great form
                // see also ValidateConfigurablePropertiesMojo.java
                element(name("directory"), "src/main/resources"), element(name("filtering"), "true")))), executionEnvironment(project, session, buildPluginManager));

        // copy the test resources
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-resources-plugin"), version("2.6")), goal("copy-resources"), configuration(element(name("outputDirectory"),
                "${project.build.directory}/ace-test"), element(name("resources"), element(name("resource"),
                // TODO hard-coding this isn't great form
                // see also ValidateConfigurablePropertiesMojo.java
                element(name("directory"), "src/test/resources"), element(name("filtering"), "true")))), executionEnvironment(project, session, buildPluginManager));


    }

    private void executeApplyBarOverrides() throws MojoFailureException, MojoExecutionException {

        try {
            getLog().info("Applying properties files as bar file overrides");
            for (File propFile : getTargetPropertiesFiles()) {

                getLog().info("  " + propFile.getAbsolutePath());

                List<String> params = new ArrayList<String>();

                // (Required) The path to the BAR file.
                params.add("-b");
                params.add(barName.getAbsolutePath());

                // (Optional) The name of the output BAR file to which the BAR file changes are to be made.
                params.add("-o");
               // String outputBarFile = new File(propFile.getParent(), propFile.getName().replaceAll("properties$", "bar")).getAbsolutePath();
                
                // Updated by Anand Awasthi
                String outputBarFile = new File(barName.getParent(), propFile.getName().replaceAll("properties$", "bar")).toString();
                outputBarFile = outputBarFile.replaceAll(".bar", "_"+versionString+".bar");
                params.add(outputBarFile);

                // (Optional) The path to one of the following resources:
                // - A BAR file that contains the deployment descriptor.
                // - A properties file in which each line contains a property-name=override.
                // - A deployment descriptor that is used to apply overrides to the BAR file.
                params.add("-p");
                params.add(propFile.getAbsolutePath());

                // (Optional) The name of an application in the BAR file
                
                // Added below code on 8/23/2018 - Updated it for policy projects for ACE v11 on 4/7/2019
                if (EclipseProjectUtils.isApplication(new File(workspace, applicationName), getLog())) {
                	params.add("-k");
                } else if (EclipseProjectUtils.isLibrary(new File(workspace, applicationName), getLog())) {
                	params.add("-y");
                }else if (EclipseProjectUtils.isPolicyProject(new File(workspace, applicationName), getLog())) {
                    	params.add("-x");
                }
                //Addition done on 8/23/2018
                
              //  params.add(getApplicationParameter());
                params.add(getApplicationName());

                // (Optional) A list of the property-name=override pairs, current-property-value=override pairs.
                // -m

                // (Optional) Specifies that all deployment descriptor files are updated recursively.
                if (applyBarOverrideRecursively) {
                    params.add("-r");
                }

                // (Optional) Specifies that the internal trace is to be sent to the named file.
                params.add("-v");
                params.add(getTraceFileParameter(propFile));

                // (Optional) The name of a library in the BAR file to which to apply overrides.
                // -y

                executeApplyBarOverride(params);               

            }
            // Delete the original bar -- Added by Anand Awasthi
            barName.delete();
            
        } catch (IOException e) {
            throw new MojoFailureException("Error applying bar overrides", e);
        }
    }

    /**
     * @return dummy comment
     * @throws MojoExecutionException If an exception occurs
     */
    private String getApplicationParameter() throws MojoExecutionException {
        String appParam = getApplicationName();
        if (versionString != null || !versionString.isEmpty()) {
            appParam = appParam + "_" + versionString;
        }

        return appParam;
    }

    /**
     * @param propFile the name of the apply bar override property file
     * @return the value to be passed to the (-v) Trace parameter on the command line
     */
    protected String getTraceFileParameter(File propFile) {
        String filename = FilenameUtils.getBaseName(applyBarOverrideTraceFile.getAbsolutePath()) + "-" + FilenameUtils.getBaseName(propFile.getName()) + ".txt";
        String directory = applyBarOverrideTraceFile.getParent();
        return new File(directory, filename).getAbsolutePath();
    }

    private String getApplicationName() throws MojoExecutionException {
        // if the application name is specified, use it
        if (applicationName != null && !applicationName.isEmpty()) {
            return applicationName;
        }

        // figure out the app name according to the naming conventions
        String artifactId = project.getArtifactId();
        String appName = artifactId.substring(0, artifactId.lastIndexOf("-bar")).concat("-app");

        // now loop through the know dependencies to see if the calculated name exists
        for (Artifact artifact : project.getDependencyArtifacts()) {

            // found it, so return it
            if (appName.equals(artifact.getArtifactId())) {
                return appName;
            }
        }

        // didn't find it, so break the build
        throw new MojoExecutionException("Unable to determine application to be overriden. Calculated name is: " + appName);
    }

    @SuppressWarnings("unchecked")
    private void validatePropertiesFiles(List<String> validProps) throws MojoFailureException {

        boolean invalidPropertiesFound = false;

        List<File> propFiles = null;
        try {
            propFiles = getTargetPropertiesFiles();
        } catch (IOException e) {
            throw new MojoFailureException("Error searching for properties files", e);
        }
        getLog().info("Validating properties files");
        for (File file : propFiles) {
            getLog().info("  " + file.getAbsolutePath());
            try {
                List<String> definedProps = FileUtils.loadFile(file);

                // check if all the defined properties are valid
                if (!validProps.containsAll(ConfigurablePropertiesUtil.getPropNames(definedProps))) {

                    getLog().error("Invalid properties found in " + file.getAbsolutePath());
                    invalidPropertiesFound = true;

                    // list the invalid properties in this file
                    for (String definedProp : definedProps) {
                        if (!validProps.contains(ConfigurablePropertiesUtil.getPropName(definedProp))) {
                            getLog().error("  " + definedProp);
                        }
                    }
                }

            } catch (IOException e) {
                throw new MojoFailureException("Error loading properties file: " + file.getAbsolutePath(), e);
            }
        }

        if (failOnInvalidProperties && invalidPropertiesFound) {
            throw new MojoFailureException("Invalid properties were found");
        }
    }

    /**
     * @param params
     * @throws MojoFailureException If an exception occurs
     */
    private void executeApplyBarOverride(List<String> params) throws MojoFailureException {

    	CommandExecutionUtil.runCommand(aceRunDir, fileTmpDir, "mqsiapplybaroverride", params, getLog());
    	
    }

    /**
     * @param params the parameters to be used with the mqsireadbar command
     * @return the screen output of the executed mqsireadbar command
     * @throws MojoFailureException If an exception occurs
     */
    private List<String> executeReadBar(List<String> params) throws MojoFailureException {
        
    	ArrayList<String> output = CommandExecutionUtil.runCommandWithOutput(aceRunDir, fileTmpDir, "mqsireadbar", params, getLog()); 

    	if (getLog().isDebugEnabled()) {
            Log log = getLog();
            for (String outputLine : output) {
                log.debug(outputLine);
            }
        }
        return output;
    }

    private void writeToFile(List<String> configurableProperties, File file) throws MojoFailureException {

        getLog().info("Writing configurable properties to: " + defaultPropertiesFile.getAbsolutePath());

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            for (String prop : configurableProperties) {
                writer.write(prop + System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            throw new MojoFailureException("Error creating configurable properties file: " + defaultPropertiesFile, e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                // ignore any error here
            }

        }

    }

    /**
     * @param output the output of the mqsireadbar command for a given bar file
     * @return a list of properties that can be overriden for a given bar file
     */
    protected List<String> getConfigurableProperties(List<String> output) {
        // extract the configurable properties

        // output format changed for ace9...

        // 1. search the output for a line indented with spaces followed by "Deployment descriptor:"
        // 2. everything after that is a configurable property up until
        // 3.a. a line less indented than the output line from "1" above
        // OR
        // 3.b. a blank line followed by "  BIP8071I: Successful command completion."
        boolean inDeploymentDescriptor = false;
        int currentIndentation = 0;

        // this could probably be done more efficiently with a subList
        List<String> configurableProperties = new ArrayList<String>();
        for (String outputLine : output) {
           
        	// Code added by Anand
        	if (outputLine.matches(" *Deployment descriptor:")) {
                inDeploymentDescriptor = false;               
            }
        	// Addition done
        	
        	if (!inDeploymentDescriptor) {
                if (outputLine.matches(" *Deployment descriptor:")) {
                    inDeploymentDescriptor = true;

                    // calculate how far indented the outputLine is
                    currentIndentation = getIndentation(outputLine);
                }
                continue;
            } 
                        
            else {
                // inDeploymentDescriptor == true, check that it hasn't ended
                if (getIndentation(outputLine) < currentIndentation) {
                    // reset and continue
                    currentIndentation = 0;
                    inDeploymentDescriptor = false;
                    continue;
                }

                if (!outputLine.trim().equals("")) {
                    configurableProperties.add(outputLine.trim());
                } else {
                    // we found a blank line - assume it's the one before
                    // "  BIP8071I: Successful command completion." and stop
                    break;
                }
            }
        }
        return configurableProperties;
    }

    /**
     * @param outputLine
     * @return dummy comment
     */
    protected int getIndentation(String outputLine) {
        return outputLine.length() - outputLine.replaceAll("^ *", "").length();
    }

    private String getCommandLine(List<String> command) {
        String ret = "";
        for (String element : command) {
            ret = ret.concat(" ").concat(element);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private List<File> getTargetPropertiesFiles() throws IOException {
        List<File> propFiles = null;

        // TODO hard-coding this isn't great form
        // see also PrepareaceBarPackagingMojo.java
        propFiles = FileUtils.getFiles(configPropFileDirectory, "*.properties", "default.properties");
        File targetaceTestDir = new File(project.getBuild().getDirectory(), "ace-test");
        if (targetaceTestDir.canRead()) {
            propFiles.addAll(FileUtils.getFiles(targetaceTestDir, "*.properties", ""));
        }

        return propFiles;
    }


}
