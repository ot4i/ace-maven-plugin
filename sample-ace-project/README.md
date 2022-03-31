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
