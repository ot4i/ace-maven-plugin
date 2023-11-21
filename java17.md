Change from 

maven-jaxb2-plugin to generate classes from xsd 
<groupId>org.apache.cxf</groupId>
<artifactId>cxf-xjc-plugin</artifactId>
<version>3.3.2</version>



# Check on versions 
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
java.lang.ClassNotFoundException: com.sun.xml.internal.bind.v2.ContextFactory
