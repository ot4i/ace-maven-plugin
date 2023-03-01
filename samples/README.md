**THIS README IS UNDER CONSTRCUTION** 


# Overview 
The sample demonstrates how a typical App Connect Enterprise application could be build using the ace-maven-plugin. 
It consists of four parts:  
 
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

**Important**: depending on the used build approach (meaning `mqsicreatebar` or `ibmint`) the build itself differs in some parts. 
See the following sections. 

# ibmint 
Building with `ibmint` has got currently* the liimtation that third party libraries defined in Java projects are not taken automatically into account (*there is another [branch](https://github.com/ChrWeissDe/ace-maven-plugin/tree/jeka) which tries to address this limitation, but still under development).
Based on this the Java projects needs to be explicitly build upfront and requires some specific extensions.      
Let's go trough it step by step: 

**Step1 : pom extension for the Java project**     
The 'trick' is that the dependencies are copied by a simple maven mechanism (maven-dependency-plugin) to the main project. From there they are then 'picked up' (automatically) by the ace-maven-plugin when building the main project.    
required changes for the Java pom.xml: 

```xml
<properties>
  <!-- relativ path to the main project--> 
  <sharedLibProject.path>../Calculator_LIB</sharedLibProject.path>
  .... 
</properties>
 
<build>
  <sourceDirectory>src</sourceDirectory>
		<outputDirectory>bin</outputDirectory>
		<plugins>
			<!-- maven-dependency-plugin to resolve the defined
dependencies and to copy them to the main project   --> 
			<plugin>
				<artifactId>maven-dependency-plugin</artifactId>
				<configuration>
					<excludeScope>provided</excludeScope>
					<excludeTransitive>false</excludeTransitive>
				</configuration>
				<executions>
					<execution>
						<phase>install</phase>
						<goals>
							<goal>copy-dependencies</goal>
						</goals>
						<configuration>
							<outputDirectory>${sharedLibProject.path}</outputDirectory>
						</configuration>
					</execution>
				</executions>
			</plugin>
   <!-- skipping the creation of the jar --> 
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<executions>
					<execution>
						<id>default-compile</id>
						<phase>compile</phase>
						<goals>
							<goal>compile</goal>
						</goals>
						<configuration>
							<skipMain>true</skipMain> <!-- used to avoid 
       that any jar is created --> 
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
```
    
**Step 2: adding java project to the build process** 
Example for a maven multi build for the SumAPI project: 

```xml
<modules>
  <module>./Java_LIB</module>
  <module>./Calculator_LIB</module>
  <module>./Sum_API</module>
   <module>./PolicyProject</module>
</modules>
```




