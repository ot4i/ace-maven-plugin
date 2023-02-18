#sample-java-project 

## About 
This sample application shows how extend an existing ACE application with an associated Java project to use the ace-maven-plugin.  
It is based on the LargeMessage sample app - available from the ACE V12 toolkit tutorials gallery.

The application itself exist of two projects: 
- LargeMessage 
- LargeMessageJava 

The first one includes a message flow that pick up a sample file based on a predefined format. 
The file is then split up by a Java Compute Node / linked Java Class into mulitple items. 
The items are then send to a predefined output folder
(defaults: input --> C:\temp-in, output --> C:\temp-out, sample file --> LargeMessages\inputSalesList.xml). 


To demonstrate the maven dependency handling the Java code was extended as follows: 

```Java
import org.apache.commons.math3.primes.Primes; 

public void ProcessLargeMessageToProduceIndividualMessages (.... 
....  
boolean isPrime = Primes.isPrime(intNumberOfSaleListsFound);
System.out.println("number of sales list found ["+intNumberOfSaleListsFound+"] is a prime: "+isPrime); 
```

The third party lib 'commons-math3' including the class 'org.apache.commons.math3.primes.Primes' is defined as dependency with the pom file. 


## Extensions LargeMessageJava project 
### pom
### .project 
### .classpath 




2.) pom.xml file 

## The Setup 
### Overview 
The sample application is build upon the IBM LargeMessage sample app - available from the ACE V12 toolkit tutorials. 
The application itself takes a large file and split it up in multiple ones. The split up itself is done by a Java Class. 
(defaults: input --> C:\temp-in, output --> C:\temp-out, sample file --> LargeMessages\inputSalesList.xml)

### Maven Config 
 

`sudo apt install maven`
### 2) Update the maven settings.xml
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

### 3) Update the pom.xml
Clone this repository.
* Update the repository urls
* Update the 'connection' string in 'scm' section

### 4) Build the plugin
If you are not doing maven release steps to release a version of the plugin, you can directly deploy the plugin locally on the build server or on to the repsotory. If doing so, make sure the remove '-SNAPSHOT' from 'version' in the pom.xml. 
Navigate to the ace-maven-plugin directory under which pom.xml is present.

* To deploy the plugin to repository: `mvn clean deploy`
* To install the plugin locally: `mvn clean install`

### 5) Using the plugin
You would need to install xvfb package on the build server (linux). Use below command to install it on ubuntu:
`sudo apt-get install -y xvfb`

There is a sample ACE maven project inside 'Sample-ace-project' directory. If your ACE project is not a maven project, first convert it to a maven project and update the POM file of the project. You may look at step 4 of below article to understand how to convert the ACE project to a maven project using toolkit.
`https://developer.ibm.com/integration/blog/2019/04/10/ibm-ace-v11-continuous-integration-maven-jenkins/`

View the Readme file of the included sample ACE project

### 6) Specific notes on plugin configuration 

**(1) Use of javaclass loaders**   
A Java Compute Node allows you to set a specific "class loader"  within the Node properties section.
Typically this will be a "Shared Lib Classloader". See for example: https://www.ibm.com/docs/en/integration-bus/10.0?topic=libraries-shared-java-files 
  
If you use this construct you have to set the plugin parameter `useClassloaders` to true. 
Otherwise the build will fail. For the syntax take a look at the sample for the pom files. 