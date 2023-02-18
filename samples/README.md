**this readme requires a reorg**


# Overview 
The sample demonstrates how a typical App Connect Enterprise application could be build using the ace-maven-plugin. 
It consists of three components:  
 
- Sum_API: a simple REST application 
- Calculator_LIB: a Shared Library 
- Java_LIB: set of Java classes 

From a dependency perspective the Sum_API requires the Calculator_LIB. In addition the Calculator_LIB itself 
uses some classes and methods from the Java_LIB project. On top the JAVA_LIB itself has got a dependency to the Apache Common Math package. 

From a deployment perpective two bar files are build and deployed to the ACE Server: 
(1) Calculator_LIB 
(2) Sum_API 


# Maven Builds 
The Maven Builds can be done via the Toolkit or a standard Command Line. 
- Prerequisites Toolkit: maven Plugin installed 

## Calculator_LIB 
The challenge is that the Calculator_LIB needs to include the JAVA_LIB as well as Apache Common Math library.
Because of this the build is done in two steps:  

**Step 1: Build of Java_LIB**  
The Java_LIB project includes an own pom.xml file. The Maven build (via the pom file) performs the following tasks:
- copy of the defined Maven dependencies (Apache Common Math) to the Calculator_LIB project directory  
- re-creation of the Eclipse project files (.classpath, .project and .settings folder)
- deletion of the .settings folder - as it causes issues during the follow-up steps. 

To perform the build, change to the **.\JAVA_LIB** folder and run the command: 
`mvn clean install` 

If you run the command within the Toolkit you have to refresh the two projects (Java_LIB, Calculator_LIB) 


**Step 2: Build of Calculator_LIB** 
The Calculator_LIB itself is using the ace-maven-plugin to create the desired bar file. 
Based the build of Step 1 the bar will include te Apache Common Math jar.  

To perform the build, change to the **.\Calculator_LIB** folder and run the command: 
`mvn clean install`  (alternative mvn clean deploy to upload the artifact directly to a central repository) 

** Alternative: combine build 
The sample folder includes also a pom sample file to run both builds at once.  
To execute the build, change to the root folder and run the following command:   
`mvn -f ./combine-java-calculator-sample-pom.xml clean install`  



## Sum_API 
The Sum_API is also using the ace-maven-plugin to create the desired bar file. 
Important: Sum_API includes a project reference to the Calculator_LIB. Thus the project needs to be available when running a 'mqsicreatebar' based build. 
However you do not have to checkout the Calculator_LIB project. Instead you can simply define a maven dependency. The ace-maven-plugin will then take to create the corresponding project in the workspace directory. 

Dependency example: 

```javascript
<dependencies>
   <dependency>
     <groupId>com.ibm.ace</groupId>
     <artifactId>Calculator_LIB</artifactId>
     <version>1.0.0</version>
     <scope>compile</scope>
     <type>zip</type> 
   </dependency>
</dependencies>
```
To perform the build, change to the **.\Sum_API** folder and run the command: 
`mvn clean install`  (alternative mvn clean deploy to upload the artifact directly to a central repository) 



# Known Limitations  
- As of today only the ace-maven-plugin only supports the dependency handling / unpacking of SharedLibs. 




# Sample project
This is a sample Rest API project that uses ace-maven-plugin for building and creating environment specific BAR files. This is a maven project.

## 'properties' folder
The 'properties' folder can contain any number of properties files. The properties files contain the environment specific values of node properties, UDPs etc. Corresponding to each properties file, an overridden BAR file will be created. This sample project contains one properties file, namely 'DEV.properties'.

## pom.xml
Notice the following configurations:
#### artifactId
Keep it as the name of the ACE project
#### packaging
packaging should be 'ace-bar'
#### plugin-version
Ensure to enter the correct version for ace-maven-plugin that you have deployed.
#### configuration properties
Keep all the properties in 'configuration' block as it is. Many of these properties have been defined in maven settings.xml and referenced here. Since these values are global for the environment, it makes sense to keep them in settings.xml instead of putting them individually in each projects' pom file.
#### distributionManagement
Define the values respective to your environment. In this case, we have considered nexus repository and the parameters defined in setting.xml have been referenced. 
#### scm values
Replace the scm configuration values respective to your environment

## Building the project
Make sure that ACE has been installed on the build server and 'settings.xml' is updated with the deployment path. Before running the maven build, make sure that xvfb has started on a specific display port

`Xvfb :99 &
export DISPLAY=:99`

Run the below command after navigating to project directory. It will compile the ACE project and create BAR file.

`mvn clean compile`

To create overridden BAR files correspoding to each properties file and upload to repository, run below command:

`mvn clean deploy`

Note that you may run the command as 'sudo' if the user does not has permissions to read/write in the workspace directory.
