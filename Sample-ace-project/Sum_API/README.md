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
Keep all the properties in 'configuration' block as it is. Many of these properties have been defined in maven setting.xml and referenced here. Since these values are global for the environment, it makes sense to keep them in settings.xml instead of putting them individually in each projects' pom file.
#### distributionManagement
Define the values respective to your environment. In this case, we have considered nexus repository and the parameters defined in setting.xml have been referenced. 
#### scm values
Replace the scm configuration values respective to your environment

## Building the project
Before running the maven build, make sure that xvfb has started on a specific display port

`Xvfb :99 &
export DISPLAY=:99`

Run the below command after navigating to project directory. It will compile the ACE project and create BAR file.

`mvn clean compile`

To create overridden BAR files correspoding to each properties file and upload to repository, run below command:

`mvn clean deploy`
