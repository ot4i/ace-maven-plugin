package ibm.maven.plugins.ace.mojos;

import ibm.maven.plugins.ace.utils.ConfigurablePropertiesUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which reads the default.properties file to figure out if the classloader approach for this bar project is consistent. 
 * Either all jar nodes in all flows must use a classloader or none of them should.
 */
@Mojo(name = "validate-classloader-approach")
public class ValidateClassloaderApproachMojo extends AbstractMojo {

    /**
     * The name of the default properties file to be generated from the bar file.
     */
    @Parameter(property = "ace.configurablePropertiesFile", defaultValue = "${project.build.directory}/ace/default.properties", required = true)
    protected File defaultPropertiesFile;

    /**
     * Whether or not to fail the build if the classloader approach is invalid.
     */
    @Parameter(property = "ace.failOnInvalidClassloader", defaultValue = "true", required = true)
    protected Boolean failOnInvalidClassloader;

    /**
     * Whether classloaders are in use with this bar
     */
    @Parameter(property = "ace.useClassloaders", defaultValue = "false")
    protected Boolean useClassloaders;

    public void execute() throws MojoFailureException {

    	/*
         * step 1 - get all properties from the default properties file 
         * (created in an earlier build step)  
         */
        List<String> configurableProperties;
        try {
            configurableProperties = readFromFile(defaultPropertiesFile);
        } catch (IOException e) {
            throw new MojoFailureException("Error reading " + defaultPropertiesFile, e);
        }

        /*
         * step 2 - debug logging of all found properties 
         * (only if debug is enabled) 
         */
        if (getLog().isDebugEnabled()) {
            getLog().debug("Configurable Properties:");
            for (String property : configurableProperties) {
                getLog().debug("  " + property);

            }
        }

        /* 
         * step 3 - get all properties ending with ".javaClassLoader" 
         * (properties from a Java Compute Node) 
         */
        List<String> clProps = ConfigurablePropertiesUtil.getJavaClassLoaderProperties(configurableProperties);

        for (String clProp : clProps) {
            // if clDefined is null, this is the first entry
            boolean clValueDefined = !"".equals(ConfigurablePropertiesUtil.getPropValue(clProp));
            if (clValueDefined != useClassloaders) {
                logInconsistency(clProps);
                if (failOnInvalidClassloader) {
                    throw new MojoFailureException("Inconsistent classloader configuration. (ace.useClassloaders = " + useClassloaders + ", classloader values defined = " + clValueDefined + ")");
                }
            }
        }
    }

    private void logInconsistency(List<String> clProps) {
        String logMsg = "Inconsistent classloader configuration. ${ace.useClassloaders} == " + useClassloaders + ". If classloaders are in use, all Java Nodes should define a classloader:";
        if (failOnInvalidClassloader) {
            getLog().error(logMsg);
            for (String string : clProps) {
                getLog().error("  " + string);
            }
        } else {
            getLog().warn(logMsg);
            for (String string : clProps) {
                getLog().warn("  " + string);
            }
        }
    }

    private List<String> readFromFile(File file) throws IOException {

        List<String> configurableProperties = new ArrayList<String>();

        getLog().info("Reading configurable properties from: " + defaultPropertiesFile.getAbsolutePath());

        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                configurableProperties.add(line);
            }
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                // ignore any error here
            }
        }

        return configurableProperties;
    }

}
