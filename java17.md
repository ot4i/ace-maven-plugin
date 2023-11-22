Change from 

maven-jaxb2-plugin to generate classes from xsd 
<groupId>org.apache.cxf</groupId>
<artifactId>cxf-xjc-plugin</artifactId>
<version>3.3.2</version>

Tested with Maven 3.9.5 
Java ibm-semeru-jdk_x64_17.0.8.1_1 


# Warnings during build 
(mvn clean install) 

## Javadoc generation 
[INFO] --- maven-javadoc-plugin:2.10.4:jar (attach-javadocs) @ ace-maven-plugin ---
[WARNING] Javadoc Warnings
[WARNING] Loading source files for package ibm.maven.plugins.ace.mojos...
[WARNING] Loading source files for package ibm.maven.plugins.ace.utils...
[WARNING] Loading source files for package ibm.maven.plugins.ace.generated.eclipse_project...
[WARNING] Loading source files for package ibm.maven.plugins.ace.generated.maven_pom...
[WARNING] Loading source files for package ibm.maven.plugins.ace_maven_plugin...
[WARNING] Constructing Javadoc information...
[WARNING] warning: The code being documented uses modules but the packages defined in http://docs.oracle.com/javase/8/docs/api/ are in the unnamed module.
[WARNING] Building index for all the packages and classes...
[WARNING] Standard Doclet version 17.0.8.1+1
[WARNING] Building tree for all the packages and classes...


## ValidateConfigurablePropertiesMojo - deprecated API 
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

## ValidateConfigurablePropertiesMojoUnitTest - same as above ? nope 
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/test/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojoUnitTest.java: c:\dev\ace-maven-plugin\jdk17\src\test\java\ibm\maven\plugins\ace\mojos\ValidateConfigurablePropertiesMojoUnitTest.java uses or overrides a deprecated API.
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/test/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojoUnitTest.java: Recompile with -Xlint:deprecation for details.
--> root cause: 
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/test/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojoUnitTest.java:[46,34] readLines(java.io.InputStream) in org.apache.commons.io.IOUtils has been deprecated
[WARNING] /c:/dev/ace-maven-plugin/jdk17/src/test/java/ibm/maven/plugins/ace/mojos/ValidateConfigurablePropertiesMojoUnitTest.java:[47,78] readLines(java.io.InputStream) in org.apache.commons.io.IOUtils has been deprecated 

# Comments von Version 
## mojo-executor
- - - - - - 
	<dependency>
			<groupId>org.twdata.maven</groupId>
			<artifactId>mojo-executor</artifactId>
			<version>2.3.3</version><!-- latest version 2.4.1-m2 by 21/11/2023; using 2.3.3 because of implementation dependencies -->
			<scope>compile</scope>
		</dependency>

  --> update to 2.4.0 / 2.4.1-m leads to compilation issues; not ciritical 

## plexus-utils
<dependency>
			<groupId>org.codehaus.plexus</groupId>
			<artifactId>plexus-utils</artifactId>
			<version>3.3.1</version><!-- latest version 4.0.0 by 21/11/2023; old: 2.0.6; using 3.3.1 because of  compatibility reasons  -->
		</dependency>

Upgrade to 4.0.0 leads to following issues (classes deprecated) 
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:compile (default-compile) on project ace-maven-plugin: Compilation failure: Compilation failure:
[ERROR] /c:/dev/ace-maven-plugin/jdk17/src/main/java/ibm/maven/plugins/ace/mojos/PackageaceSrcMojo.java:[84,148] cannot access org.codehaus.plexus.util.xml.Xpp3Dom
[ERROR]   class file for org.codehaus.plexus.util.xml.Xpp3Dom not found
[ERROR] /c:/dev/ace-maven-plugin/jdk17/src/main/java/ibm/maven/plugins/ace/mojos/PackageaceSrcMojo.java:[84,9] cannot access org.codehaus.plexus.util.xml.Xpp3Dom


## jaxb-runtime
<dependency>
			<groupId>org.glassfish.jaxb</groupId>
			<artifactId>jaxb-runtime</artifactId>
			<version>4.0.4</version><!-- 2.3.2 --> 
			<scope>runtime</scope>
		</dependency>

## cxf-xjc-plugin

<plugin>
				<groupId>org.apache.cxf</groupId>
				<artifactId>cxf-xjc-plugin</artifactId>
				<version>3.3.2</version>
using new version 4.0.0 leads to: 
  package jakarta.xml.bind.annotation does not exist
  
java.lang.ClassNotFoundException: com.sun.xml.internal.bind.v2.ContextFactory



