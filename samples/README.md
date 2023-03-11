# Overview 
The sample  demonstrate how to build a typical ACE application. It consists of four componente:  
 
- Sum_API: a simple REST application 
- Calculator_LIB: a Shared Library 
- Java_LIB: set of Java classes
- PolicyProject: set of simple ACE policies

From a dependency perspective the Sum_API requires the Calculator_LIB. In addition the Calculator_LIB itself 
uses some classes and methods from the Java_LIB project. On top the JAVA_LIB itself has got a third party dependency to the Apache Common Math package. 

From a deployment perpective three bar files are build and deployed to the ACE Server: 
(1) Calculator_LIB 
(2) Sum_API 
(3) PolicyProject

**Important**: 
* depending on the used build approach (meaning `mqsicreatebar` or `ibmint`) the build itself differs in some aspects. See the following sections for details. In general the Java project is build together with the SharedLib. Thus it does not required a dedicated build.    
* each project contains a **pom example** for `mqsicreatebar` or `ibmint`.   
* typical build order for both approaches is Calculator_LIB (Shared Library), Sum_API (Rest Application), PolicyProject 

# ibmint 
Building with `ibmint` is quite straight forward. As outlined in the pom you just have to set the parameter "ibmint" to "true". 
```
<ibmint>true</ibmint>
```
In addition you can set all additional ibmint parameters as `doNotCompileJava` or `compileMapsAndSchemas` (see also https://www.ibm.com/docs/en/app-connect/12.0?topic=commands-ibmint-package-command). However easiest way is to go with the defaults. 

Under the cover the ace-maven-plugin does some magic as it 
* resolves the required Java projects, and add them to the build (via additional --project flag)    
* adds the maven dependencies defined in the Java projects to the resulting bar file 

However ibmint is only supported for  ACE runtimes > version 12.0.6. In addition it only covers typical "project setups" at the moment. There might be special cases where ibmint might fail.   

# mqsicreatebar  
Building with mqsicreatebar is also straight forward. However mqsicreatebar has some drawbacks as it 
* runs a headless Eclipse under the cover - and thus requires a X-Window on Linux  
* requires that all related project are 'available' (checked by Eclipse) 
* is quite slow - due the Eclipse startup 

However it takes care about all your project and maven dependencies. Thus it might work better for complex or 'unusual' project setups. Further it is the only supported build mode for legacy IIB runtimes (from a perspective of this plugin).   

As described above mqsicreatebar requires that all dependent project are "available in the workspace". For this reason the Sum_API project defines a dedicated maven dependency for the Calculator_LIB: 

```
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
**Important**:  scope needs to be set to compile and type to zip. Based on this dependency the ace-maven-plugin downloads and extracts the shared lib into the workspace. Without this dependency the build will fail, as the project is not available. 




