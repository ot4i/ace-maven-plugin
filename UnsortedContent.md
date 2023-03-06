# THIS FILE CONTAINS UNSORTED CONTENT 

# UNSORTED CHANGES 

## How to Setup Linux Jenkins Environment 

## Steps to build the plugin

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


===========

--> FÜR die SampleApp README.md !! 
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


## 'properties' folder
The 'properties' folder can contain any number of properties files. The properties files contain the environment specific values of node properties, UDPs etc. Corresponding to each properties file, an overridden BAR file will be created. This sample project contains one properties file, namely 'DEV.properties'.

## pom.xml
Notice the following configurations: