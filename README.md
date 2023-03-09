# About 
This plugin can be used to build IBM App Connect Enterprise projects. Result is typically a bar file which can be deployed to an IBM Integration Server.
The project supports two build modes: 'mqsicreatebar' or 'ibmint'. Details see the section "How to use the plugin". 
The current version of the plugin was tested with IBM App Connect Enterprise 12.0.6.  
    
**Important**: the code is provided in 'good faith' and AS-IS. There is no warranty or further service implied or committed. Any supplied sample code is not supported via IBM product service channels.
Feel free to fork the code and do your own adjustments. Of course contributions are always welcome (e.g. via merge requests). 


# Changes
Current version of the plugin is "12.0.6 [-SNAPSHOT]". 
Following changes (compared to the last major update in 2022): 

- support for additional buildmode "ibmint" 
- updated sample (incl pom templates for ibmint and mqsicreatebar) 
- support for additional source packaging (configurable via pom) 
- update for bar override handling (allows multiple properties, keeps original file)
- update of used maven dependencies (to latest possible version) 
- general code fixes, cleanup and optimization 

# How to build the plugin 
You have to build the plugin on our own. There is no version available on Maven central or any other repository. 
However the steps to build the plugin are quite simple - so no worries. 

First thing is of course to install maven and java if not done already. E.g. on Linux via 
* `sudo apt-get install openjdk-8-jdk`   
* `sudo apt install maven`   

For Java it is important to use  **JDK version 8**. Reason is that the JAXB libraries were "reorganized" for Java 9 and higher. Thus building with Java 9 and above will result in errors.      

Next steps it to update the  maven settings.xml files to your needs (e.g. repositories etc.). 

If you are not doing maven release steps to release a version of the plugin, you can directly deploy the plugin locally on the build server or on to the repository. If doing so, make sure the remove '-SNAPSHOT' from 'version' in the pom.xml. 
Navigate to the ace-maven-plugin directory under which pom.xml is present.

* To deploy the plugin to repository: `mvn clean deploy`
* To install the plugin locally: `mvn clean install`
 

# How to use the plugin 
* In order to use the plugin your projects need to be "mavenized". Easiest way is to add a pom to your project and add the following buildCommand and nature to your project: 
```
<buildSpec>
  ...
  <buildCommand>
     <name>org.eclipse.m2e.core.maven2Builder</name>
     <arguments>
     </arguments>
  </buildCommand>
</buildSpec>
... 
<natures>
  ...
  <nature>org.eclipse.m2e.core.maven2Nature</nature>
</natures>
```   	
* for pom templates see the sample (there are different poms for ibmint or mqsicreatebar) 
* When using an "old toolkit" - meaning ACE version 11 or still IIB -  you have to install maven in the toolkit (Eclipse) manually. Following article provides detailed instructions: see https://developer.ibm.com/integration/blog/2019/04/10/ibm-ace-v11-continuous-integration-maven-jenkins/ 


As mentioned before there are **two 'build options'**     
a) mqsicreatbar    
b) ibmint    


## Differences between mqsicreatebar and ibmint 

**mqsicreatebar**
* uses headless eclipse under the cover (includinge maven e2 plugin) 
* same behaviour as build via ACE toolkit 

advantages:    
* build as done by the ACE toolkit 
* can be used for any IIB or ACE version 
* automatic handling of maven mechanismen based on m2e Eclipse project (including dependent projects!) 
* full support for mqsireadbar 
* automatic handling of .project  - might be required for more complex scenario 

disadvantages:    
* requires that all related project are 'available' (checked by Eclipse)  
* requires an X-window on linux (to startup the headless Eclipse) 
* build quite slow 

**ibmint**
* new packaging / build command 
* basic depency management / project discovery provided by ace-maven plugin      
   
advantages:    
* faster than mqsicreatebar 
* does not require projects from .project file 
* no X-window required     

disadvantages: 
* only supported for ACE > version 12.0.6 
* some limitations related to mqsireadbar

following use cases were tested with ibmint: 
* SharedLib with related Java project 
* Standard and REST applications   
* Policy Projects 


# What you should know and Lessons Learned 
* following [Readme](LinuxSetup.md) explains how to setup a Jenkin build job with the ace-maven-plugin on Linux: 
* because of historical reasons the plugin itself supports further build modes like ace-par, ace-classloader and ace-src. However those build modes were NOT tested in the current release. However they will likely work.     
* for ibmint the ace-maven-plugin performs for ibmint the following additional steps to ensure a proper build: 
	* to scan the project for dependent Java projects. If found:  
		* add the java project to the build (via additional --project entry) 
		* copy additional maven dependencies to the main project and update the compile classpath (via MQSI_EXTRA_BUILD_CLASSPATH) 
* ibmint requires a folder / file access and uses therefore the MQSI_WORKPATH. To avoid any issues on the build server the ace-maven-plugin creates a temporay Workpath under {project.build.directory}/tmp-work-dir. The folder can be changed by adding the config parameter mqsiTempWorkDir to the pom.xml. 
* in general the environment variable "MQSI_EXTRA_BUILD_CLASSPATH" can be used to add additional jars to the ibmint build process (for ACE > version 12.0.6)
* both build modes could be also used to **verride the properties** of the bar file. The 'properties' folder (can be configured within the pom - see samples) can contain any number of properties files.  Corresponding to each properties file, an overridden BAR file will be created.    
* Be careful and do not list (refer) "maven dependencies" as additional jar files in the .classpath files. This will break the maven dependency mechanism. Example for a wrong setup with commons-math3-3.5.jar: 
```
	.classpath Datei 
	<!-- zusätzliche classapth Entry -- die common-maths wird nicht in die Shared Lib übernommen --> 
	<classpathentry kind="var" path="M2_REPO/org/apache/commons/commons-math3/3.5/commons-math3-3.5.jar"/>
	 
	<classpathentry kind="con" path="com.ibm.etools.mft.uri.classpath.MBProjectReference"/>
	<classpathentry kind="lib" path="C:/Program Files/IBM/ACE/12.0.6.0/server/classes/javacompute.jar"/>
	<classpathentry kind="lib" path="C:/Program Files/IBM/ACE/12.0.6.0/server/classes/jplugin2.jar"/>
	<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
		<attributes>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	
	.pom Datei 
	
	<dependency>
		<groupId>org.apache.commons</groupId>
		<artifactId>commons-math3</artifactId>
		<version>3.5</version>
	</dependency>
		<dependency>
    	<groupId>commons-io</groupId>
    	<artifactId>commons-io</artifactId>
    	<version>2.10.0</version> <!-- latest version 2.11.0 by 31/03/2022; added -->
	</dependency>
```

* try to avoid any system or provided scope dependencies. This typically result in issues when handling the project within the ACE toolkit or the mqsicreatebar build. 
* for any further development:
	* in some cases I had the need to extend the standard maven build classpath. But there is NO way to manipulate it via a pom / plugin config. So save (use) your time for other stuff .... ; ) 



# Further Ideas 
* incooperate TestApplication changes from Thomas Mattsson (https://github.com/thomas-mattsson)
* include parameter to define if 'tmp files' should be kept 
* optimize CommandExecutionUtil / ProcessOutputCatcher / ProcessOutputLogger 
* update/rewrite validate bar workspace logic (e.g. ensure that only the required projects are in place)

