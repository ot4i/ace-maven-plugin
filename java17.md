# Overview
The plugin was updated to Java 17. This means that the plugin itself can be now now compiled and used with Java 17.  

The plugin  was tested with: 
- Maven 3.9.5
- IBM Semeru JDK 17.0.8.1 

Important: the Java 17 upgrade does NOT affect the build of the ACE java application. Here you are still bound to either Java 8 (default) or Java 11 as supported runtimes. A good article how to handle the Java runtimes for the ACE application build can be found here: 
- https://github.com/trevor-dolby-at-ibm-com/ace-java11-qpid-demo

# Major changes 
- move from the maven-jaxb2-plugin to the org.apache.cxf to autogenerate the classes for Eclipse project and Maven pom handling. 
- update to the latest/stable dependencies and plugins (when possible). See the main pom for comments. 
- removed of the maven-scm-plugin and maven-release-plugin as they are not used

# Comments regarding used versions

## org.twdata.maven:mojo-executor
- using 2.3.3, latest: 2.4.1-m2
- the latest version results in compilation issue, because of deprecated/changed classed and method. 

## org.codehaus.plexus:plexus-utils
- using: 3.3.1, latest 4.0.0
- the latest version results in compilation issue, because of removed classes (e.g. org.codehaus.plexus.util.xml.Xpp3Dom) 
	
## org.glassfish.jaxb:jaxb-runtime
- using 2.3.2, latest: 4.0.4 
- the latest version results in compilation issue, because of deprecated/changed classed and method.

## org.apache.cxf:cxf-xjc-plugin
- using 3.3.2, latest: 4.0.0 
- the latest version results in compilation issue; as
    - package jakarta.xml.bind.annotation was reoved
    - class com.sun.xml.internal.bind.v2.ContextFactory was removed (java.lang.ClassNotFoundException) 
 
# Open points 
Following topics requires an update/cleanup  

## ValidateConfigurablePropertiesMojo - deprecated API 
reported during build (mvn clean install) 
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/main/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojo.java: c:\dev\ace-maven-plugin\jdk17\src\main\java\ibm\maven\plugins\ace\mojos\ValidateConfigurablePropertiesMojo.java uses or overrides a deprecated API.
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/main/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojo.java: Recompile with -Xlint:deprecation for details.
--> root cause: 
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/main/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojo.java:[323,49] getDependencyArtifacts() in org.apache.maven.project.MavenProject has been deprecated


##  maven-plugin-plugin - wrong scope 
--> issue mit dem Plugin? 

[INFO] --- maven-plugin-plugin:3.10.2:descriptor (default-descriptor) @ ace-maven-plugin ---
[WARNING]

Some dependencies of Maven Plugins are expected to be in provided scope.
Please make sure that dependencies listed below declared in POM
have set '<scope>provided</scope>' as well.

## ValidateConfigurablePropertiesMojoUnitTest  
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/test/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojoUnitTest.java: c:\dev\ace-maven-plugin\jdk17\src\test\java\ibm\maven\plugins\ace\mojos\ValidateConfigurablePropertiesMojoUnitTest.java uses or overrides a deprecated API.
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/test/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojoUnitTest.java: Recompile with -Xlint:deprecation for details.
--> root cause: 
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/test/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojoUnitTest.java:[46,34] readLines(java.io.InputStream) in org.apache.commons.io.IOUtils has been deprecated
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/test/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojoUnitTest.java:[47,78] readLines(java.io.InputStream) in org.apache.commons.io.IOUtils has been deprecated 

