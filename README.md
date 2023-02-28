
**THIS README IS UNDER CONSTRUCTION** 

# About 
This plugin can be used to build IBM App Connect Enterprise projects. Result is typically a bar file which can be deployed to an IBM Integration Server.
The project itself can be build based on 'mqsicreatebar' or 'ibmint'. Details see the section "How to use the plugin". 
The current version of the plugin was tested with IBM App Connect Enterprise 12.0.6.  
**Important**: the code is provided in 'good faith' and AS-IS. There is no warranty or further service implied or committed. Any supplied sample code is not supported via IBM product service channels.
Feel free to fork the code and do your own adjustments. Of course contributions are always welcome (e.g. via merge requests). 


# Changes
Current version of the plugin is "12.0.6 [-SNAPSHOT]". 
Following changes (compared to the last major update in 2022): 

- support for alternative build via ibmint 
- updated samples (for ibmint and mqsicreatebar) 
- fixes for maven dependencies handling 
- additional source packaging (configurable via pom) 
- updated bar override handling (allows multiple properties, keeps original file)
- harmonized  mqsi commands (incl logging)
- update of used maven dependencies (to latest possible version) 
- general code cleanup and optimization 

# How to build the plugin 
You have to build the plugin on our own. There is no version available on Maven central. 
However the steps to build the plugin are quite simple - so no worries. 

Here the sample instructions for a build on Linux (Ubuntu 18.04) with a Nexus repository. Changes to other environments should be quite easy:   

**1) Install maven on build server**   
Ensure that you have JDK8 installed

`sudo apt-get install openjdk-8-jdk`

Install Maven by typing the following command:

`sudo apt install maven`

**2) Update your maven settings**   
Edit the settings.xml located in 'conf' folder under maven home directory. You can copy the sample 'setting.xml' included here and make necessary changes.
* Update the 'localRepository' if not using the default location
* Enter the credentials for nexus repository that has access to deploy artifacts to the repository
`<server>
    <id>releases</id>
    <username>nexus-deployer</username>
    <password>Passw0rd</password>
 </server>`
* Update the profile properties and repository locations
* Update the 'eclipse.workspace' and 'perform.workspace' values as per your environment. You may keep these values if you are using Jenkins with home directory '/var/lib/jenkins'. Also if you are using jenkins, you may need to update {JENKINS_HOME_DIR}/config.xml to have below values:
`<workspaceDir>${ITEM_ROOTDIR}/workspace</workspaceDir>
 <buildsDir>${ITEM_ROOTDIR}/builds</buildsDir>`

**3) Build the plugin**   
If you are not doing maven release steps to release a version of the plugin, you can directly deploy the plugin locally on the build server or on to the repository. If doing so, make sure the remove '-SNAPSHOT' from 'version' in the pom.xml. 
Navigate to the ace-maven-plugin directory under which pom.xml is present.

* To deploy the plugin to repository: `mvn clean deploy`
* To install the plugin locally: `mvn clean install`


# How to use the plugin 
- see primary sample SumAPI and Readme 
- link: https://github.com/ChrWeissDe/ace-maven-plugin/blob/main/samples/README.md 

- to 'build options' 
a) mqsicreatbar 
b) ibmint 

Differences: 

**mqsicreatebar**
* uses headless eclipse under the cover (includinge maven e2 plugin) 
* same behaviour as build via ACE toolkit 

advantages:    
* build as done by the ACE toolkit 
*  automatic handling of maven mechanismen based on m2e Eclipse project
* full support for mqsireadbar 
* automatic handling of .project  - might be required for more complex scenario 

disadvantages:    
* requires that all related project are 'available' - check by Eclipse 
* build quite slow - as the Eclipse need to be started up 

**ibmint**
* new packaging / build command 
advantages:    
* faster than mqsicreatebar 
* does not require projects from .project file 
* inbuild support for simple use cases 
* java projects directly referenced by the library 

disadvantages: 
* requires specific pom for related Java Projects - see https://github.com/ChrWeissDe/ace-maven-plugin/blob/main/samples/README.md  
* some limitations related to mqsireadbar 


# What you should know 
--> TODO: Section about relevant content  
--> link to Setup on Liniux --> LinuxSetup.md
--> build-with-ibmint.md 
	- MQSI_WORKPATH 
	- MQSI --> additional classpath - automatic handling for ibmint 
	

# Further Ideas 
* update/rewrite validate bar workspace logic (e.g. ensure that only the required projects are in place)
* incooperate TestApplication changes from Thomas Mattsson (https://github.com/thomas-mattsson)
* optimize CommandExecutionUtil / ProcessOutputCatcher / ProcessOutputLogger 
* include parameter to define if 'tmp files' should be kept 

