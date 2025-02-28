# Overview
The plugin was updated to Java 17. This means that the plugin itself can be now now compiled and used with Java 17.  

The plugin  was tested with: 
- Maven 3.9.2
- IBM Semeru JDK 17.0.8.1 

Important: the Java 17 upgrade does NOT affect the build of the ACE java application. Here you are still bound to either Java 8 (default) or Java 11 as supported runtimes. A good article how to handle the Java runtimes for the ACE application build can be found here: 
- https://github.com/trevor-dolby-at-ibm-com/ace-java11-qpid-demo

# Major changes 
- update to the latest/stable dependencies and plugins (when possible). See the main pom for comments. 
- removed of the maven-scm-plugin and maven-release-plugin as they are not used

# Comments regarding used Maven plugin versions

## org.twdata.maven:mojo-executor
- using 2.3.3, latest: 2.4.1-m2
- the latest version results in compilation issue, because of deprecated/changed classed and method. 
	
## junit:junit
- using junit:junit component  
- package was moved to junit-jupiter-api 
- still using junit:junit, as the build with junit-jupiter-api fails 

## jakarta.xml.bind:jakarta.xml.bind-api
- using 3.0.1, latest 4.0.2
- using 3.0.1 because of documentation, pointing out to use this specific version 
- not tested the latest version yet 

## org.glassfish.jaxb:jaxb-runtime
- using 3.0.2, latest: 4.0.5 
- using 3.0.2 because of documentation, pointing out to use this specific version 
- not tested the latest version yet 

# Maven Plugins used in the code 
To execute some specific tasks the ace-maven-plugin itself uses within the source code the following Maven plugins: 

- maven-dependency-pluging:3.8.1
- maven-resource-plugin:3.3.1
- maven-jar-plugin:3.4.2 
- maven-assembly-plugin:3.7.1 
- maven-source-plugin:3.3.1 
- build-helper-maven-plugin:3.6.0 

# Hints and Tips 

You can use the following maven properties to show details on validation, warnings and deprecations: 
 -Dmaven.plugin.validation=VERBOSE  -Dmaven.compiler.showWarnings=true -Dmaven.compiler.showDeprecation=true 

# Open points 

Following 'issues' are reported when building the plugin. 

## WARNING: deprecated classes and methods 
Following classes and methods are reported as deprecated: 

- org.apache.maven.plugins.annotations.Component in org.apache.maven.plugins.annotations
- getDependencyArtifacts() in org.apache.maven.project.MavenProject

## WARNING: some dependencies of Maven Plugins are expected to be in provided scope
The plugin:3.15.1:descriptor (default-descriptor) @ ace-maven-plugin reports that "some dependencies of Maven Plugins are expected to be in provided scope"- The plugin seems to check 'auto generated' pom files. 

Thus there is nothing we can currently do about the warnings. 

Only option would be to surpress the warnings; see the parameter <checkExpectedProvidedScope> of the descriptor mojo (https://maven.apache.org/plugin-tools/maven-plugin-plugin/descriptor-mojo.html). 

## org.apache.cxf:cxf-xjc-plugin:4.1.0
- Plugin depends on the deprecated Maven 2.x compatibility layer, which may not be supported in Maven 4.x

Comments/Reason: no update for the plugin available at the moment
