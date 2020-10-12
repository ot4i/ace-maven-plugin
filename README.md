# ace-maven-plugin
## About
This plugin can be used to build IBM App Connect Enterprise projects and create BAR files for deployment. You may install the plugin locally or can deploy it on the Enterprise repository server, which can be pulled during maven build of the ACE projects. We have included a sample 'settings.xml' file for maven instance, which makes use of Nexus as repository server. You should update the 'settings.xml' with the values appropriate for your environment.
The plugin also contains a 'pom.xml'. Ensure to update the repository server url and other values appropriate to environment before building the plugin.

## Steps to build the plugin
Below are the steps to build the plugin. The provided instructions have been tested in Linux (Ubuntu 18.04) with Nexus repository. You can make necessary changes if using a different repository server.

### 1) Install maven on build server
Ensure that you have JDK8 installed

`sudo apt-get install openjdk-8-jdk`

Install Maven by typing the following command:

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

### 3) Update the pom.xml
Clone this repository.
* Update the repository urls
* Update the 'connection' string in 'scm' section

### 4) Build the plugin
If you are not doing maven release steps to release a version of the plugin, you can directly deploy the plugin locally on the build server or on to the repsotory. If doing so, make sure the remove '-SNAPSHOT' from 'version' in the pom.xml. 
Navigate to the ace-maven-plugin directory under which pom.xml is present.

* To deploy the plugin to repository: `mvn clean deploy`
* To install the plugin locally: `mvn clean install`
