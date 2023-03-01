# THIS FILE CONTAINS UNSORTED CONTENT 

#ibmint content 

Es ist wichtig zu verstehen, daß es zwischen den zwei Modi "mqsicreatebar" und "ibmint" die folgende Unterschiede gibt: 

**Scenario 1 - Build im Toolkit** 
- Verwendung von Eclipse, m2e Plugin und mqsicreatebar
- .project Abhängigkeiten werden aufgelöst (Eclipse Feature) 
- maven dependencies vom Java Projekt werden mitgenommen (über "Maven Builder" in .project Datei; Intepretation der .classpath Einträge) 

**Scenario 2 - Build (Server oder Lokal) mit mqsicreatebar**   
- Da mqsicreatebar ein Headless Eclipse aufruft, werden hier wieder Eclipse, m2e Plugin und mqsicreatebar verwendet 
- .project Abhängigkeiten werden aufgelöst
- maven dependencies vom Java Projekt werden mitgenommen (über "Maven Builder" in .project Datei; Intepretation der .classpath Einträge) 

**Scenario 3 - Build (Server oder Lokal) mit ibmint**  
- Es wird hier KEIN Headless Eclipse verwendet 
- Das bedeutet auch das .project Abängigkeiten nicht aufgelöst werden
- maven dependencies vom Java Projekt werden nicht mitgenommen (über .classpath Einträge)

## SharedLib mit Java Projekt über ibmint bauen 
Um aktuell das gleiche Ergebnis wie mit mqsicreatebar und Eclipse zu bekommen ist aktuell folgender Ansatz implementiert: 

1.) Build vom Java Projekt 
- Build Schritt besteht nur aus der Kopie der Dependencies in das "SharedLib Hauptprojekt"   
- keine Kompilierung der eigentlichen Java Klassen
- Beispiele siehe: https://github.com/ChrWeissDe/ace-maven-plugin/tree/feature/ibmint-only/sample-ace-project/Java_LIB 


2.) Build der SharedLibrary 
- über ACE Maven Pluging (Version im ibmint-only branch 
- zusätzliche Parameter "ibmintDependencies" und "addJars" 
- Parameter "ibmintDependencies": Angabe vom Java Projekt - wird dann mit --project in den ibmint Aufruf übernommen 
- Parameter "addJars": scannt das SharedLibrary Projekt und nimmt die jars in die Umgebungsvariable MQSI_EXTRA_BUILD_CLASSPATH auf; damit ist sichergestellt, dass alle Klassen kompiliert werden können. 
- Beispiel: https://github.com/ChrWeissDe/ace-maven-plugin/blob/feature/ibmint-only/sample-ace-project/Calculator_LIB/pom.xml   
(Achtung ibmint muss im Beispiel noch auf "true" gesetzt werden)   
 
## What you should know 
ibmint benötigt temporär einen Dateizugriff und verwendet dafür den MQSI_WORKPATH
Da dieser typischerweise nicht zur Verfügung steht - erstellt das ace-maven-plugin einen (temporären) Workpath unter {project.build.directory}/tmp-work-dir.
Ein alternatives Verzeichnis kann in der pom über den Parameter mqsiTempWorkDir gesetzt werden. 


## Gut zu wissen 
**Issue: zusätzliche Einträge in .classpath können verhindern, das Maven Dependencies richtig übernommen werden**   
- Ausgangslage ist eine SharedLib mit einem zugehörigen Java Projekt   
- Wie in Scenario 1 und 2 beschrieben, werden die Maven Dependencies vom Java Projekt übernommen   
- Dies erfolgt unten über den Eintrag MAVEN2_CLASSPATH_CONTAINER und name="maven.pomderived" value="true"   
- Wenn die gleiche Library einer Dependency aber zusätzlich in der .classpath Datei referenziert wird - (siehe Eintrag für commons-math3-3.5.jar), wird diese NICHT über den Maven Mechanismus in der SharedLibrary abgelegt. 

```
	.classpath Datei 
	<!-- zusätzliche classapth Entry -- die common-maths wird nicht in die Shared Lib übernommen --> 
	<classpathentry kind="var" path="M2_REPO/org/apache/commons/commons-math3/3.5/commons-math3-3.5.jar"/>
	 
	<classpathentry kind="con" path="com.ibm.etools.mft.uri.classpath.MBProjectReference"/>
	<classpathentry kind="lib" path="C:/Program Files/IBM/ACE/12.0.6.0/server/classes/javacompute.jar"/>
	<classpathentry kind="lib" path="C:/Program Files/IBM/ACE/12.0.6.0/server/classes/jplugin2.jar"/>
	<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
		<attributes>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	
	.pom Datei 
	
	<dependency>
		<groupId>org.apache.commons</groupId>
		<artifactId>commons-math3</artifactId>
		<version>3.5</version>
	</dependency>
		<dependency>
    	<groupId>commons-io</groupId>
    	<artifactId>commons-io</artifactId>
    	<version>2.10.0</version> <!-- latest version 2.11.0 by 31/03/2022; added -->
	</dependency>
```



## Backup - zu sortieren 

### ibmint 

Aktuelle unterstützte Use Cases: 
- Shared Lib mit Java Anteil - Java Part nur mit Maven Dependencies     
- Application, REST APIs (ohne Java Anteil / Java Calls) 
- Policy Projects



### Weiterer Punkte 

Ausgabe von aktuellen Classpath: 
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt


ibmint would normally use the .classpath file in the Java project for the classpath when building, and should handle the various different entries in that file these days (early v12 fixpacks might not handle all of them).
It's possible that some entries still aren't being handled properly (would be good to know what's not working) but MQSI_EXTRA_BUILD_CLASSPATH can add additional JARs as a workaround.

## Verprobte Optionen 


Optionen: 
(1) Build der Java Projekte mit "Standard Maven Build", Bau von SharedLib via ibmint 
Vorraussetzung: Externe Abhängigkeiten müsen in den "Compile Classpath von Maven" mit aufgenommen werden 


(1a) Erweiterung Maven Build Path via "System Scope Dependencies" 
- einfachste Variante 
- Hinterlegung der jars als "System Scope" mit Angabe vom "System Path" 
siehe auch: https://stackoverflow.com/questions/2479046/maven-how-to-add-additional-libs-not-available-in-repo
- **verprobt, funktioniert** 
- wird tlw. als "bad practice" angesehen, da es anscheinend bei Erstellung von "Maven Assemblies" zu Problemen kommen kann
- zu prüfen, ob wird das in unserem Fall vernachlässigen können. 
- aktuell auch als "deprecated" gemarkt, unklar aber wann es wirklich deprecated wird 
--> funktioniert am Ende aber nicht; da im Toolkit die Dependencies mit in die bar aufgenommen werden ....   

(1b) Erweiterung Maven Build Path via "Provided Scope Dependencies"
- analog zu 1a) ; Dependencies müssen aber zumindest im "lokal Maven" hinterlegt sein 
- generell möglich die Installation ins lokal Maven mit in den Build Job aufzunehmen 
(Maven Repository auf dem Build Server) 
- Alternative: generelle Hinterlegung der Dependencies auf dem zentralen Nexus 
--> funktioniert am Ende aber nicht; da im Toolkit die Dependencies mit in die bar aufgenommen werden ....     


(1c) Modifizierung Erweiterung des Java Classpaths via compilerArgs Argument 
- der Java Classpath kann leider nicht direkt gesetzt werden 
- Workaround: Ausgabe Java Classpath in Datei, einlesen, modifizieren und setzen als "compilerArgs" 
- siehe: https://stackoverflow.com/questions/3410548/maven-add-a-folder-or-jar-file-into-current-classpath
--> verprobt - funktioniert aber nicht; der -classpath Parameter wird gesetzt, ist aber dann doppelt vorhanden; dies führt dann zu Problemen   


(2) Build von Shared Lib mit Java Projekt nur über "ibmint" 
- gemeinsamer Bau der Shared und Java Lib via ibmint (zusätzlicher Eintrag in der pom.xml) 
- initial verprobt 
- issues: Maven Dependencies werden nicht korrekt aufgelöst
- Maven Eintrag: <classpathentry kind="var" path="M2_REPO/org/apache/commons/commons-math3/3.5/commons-math3-3.5.jar"/> 
- Dependencies müssen in classpath als "lib" Eintrag (während dem Build) angepasst werden 
- weiter Verprobung notwendig 
- weiterer Gedanke: Java Projekt als Hauptprojekt --> damit Zugriff auf die pom Dependencies ? 
--> aktueller Ansatz der weiter verfolgt wird; erster Prototyp / Ansatz siehe oben 


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

#ibmint options 
3 Optionen: 

1.) Ohne Java Pom 

via Jeka Toolkit 
issue: 
- download Pfad für ivy.jar fest hinterlegt  
- Ticket geöffnet 

2.) Mit Java Pom 

a) mit copy Dependencies und auslesen der dependencies 
--> benötigt ein build vor dem eigentlichen Projekt 
- liste der jar Dateien aus dem Hauptverzeichnis 


b) mit pom dependency 
--> benötigt ein build vor dem eigentlichen Projekt
- liste der jar Dateien aus dem dependency tree 

===========
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


