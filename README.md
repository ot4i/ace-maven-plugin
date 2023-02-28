# About this branch 

- this branch uses jeka for the automatic dependency resolution when building with ibmint 
- in general 'working' - but has got the limitation that it requires a direct "internet connection" 
- reason: jeka uses under the cover ivy - and downloads the ivy dependencies directly from maven central 
- see following jeka issue: https://github.com/jeka-dev/jeka/issues/235



--- original content 

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
If your run on Linux you have to install the xvfb package and open a display. Run e.g. the following commands on unbuntu (make sure to select the correct bashrc file depending on the 'build user'): 

- `sudo apt-get install -y xvfb`
-  echo "Xvfb -ac :100 &" >> /root/.bashrc
-  echo "export DISPLAY=:100" >> /root/.bashrc

In the case of RHEL you have to install further packages. Follow the instructions here: https://www.ibm.com/support/pages/node/6823669

There is a sample ACE maven project inside 'Sample-ace-project' directory. If your ACE project is not a maven project, first convert it to a maven project and update the POM file of the project. You may look at step 4 of below article to understand how to convert the ACE project to a maven project using toolkit.
`https://developer.ibm.com/integration/blog/2019/04/10/ibm-ace-v11-continuous-integration-maven-jenkins/`

View the Readme file of the included sample ACE project

### 6) Specific notes on plugin configuration 

**(1) Use of javaclass loaders**   
A Java Compute Node allows you to set a specific "class loader"  within the Node properties section.
Typically this will be a "Shared Lib Classloader". See for example: https://www.ibm.com/docs/en/integration-bus/10.0?topic=libraries-shared-java-files 
  
If you use this construct you have to set the plugin parameter `useClassloaders` to true. 
Otherwise the build will fail. For the syntax take a look at the sample for the pom files. 
