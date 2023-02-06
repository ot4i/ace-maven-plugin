## ibmint 

Aktuelle unterstützte Use Cases: 
- Shared Lib mit Java Anteil - Java Part nur mit Maven Dependencies     
- Application, REST APIs (ohne Java Anteil / Java Calls) 
- Policy Projects


## Handling Java Classpath bei Shared Libraries   

Ausgangslage: 
Shared Library mit einem verlinkten Java Projekt.   
Das Java Projekt beinhaltet Maven Dependencies und Abhängigkeite zu den "App Connect Enterprise Standard Klassen" (bspws. jplugin.jar und javacompute.jar aus <ace-install-dir>/server/classes). 

Ziel:    
Bau der  "Shared Library" als bar Datei.  
Inhalt: Java Projekt (als Jar) plus die Maven Dependencies (als einzelne Jars)    

Problem: unterschiedliche Verhaltensweise bei mqsicreatebar und ibmint. 

Bei mqsicreate wird über die Eclipse Umgebung **beide Abhängigkeiten** korrekt aufgelöst, und die bar Datei korrekt gebaut 
Bei ibmint werden die Maven Dependencies nicht aufgelöst, damit schlägt die Kompilierung fehl. 
Bei einen Maven Build sind die externen Abhängigkeiten (Standard App Connect Enterprise Klassen) (aktuell) nicht im Classpath enthalten. 



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

(1b) Erweiterung Maven Build Path via "Provided Scope Dependencies"
- analog zu 1a) ; Dependencies müssen aber zumindest im "lokal Maven" hinterlegt sein 
- generell möglich die Installation ins lokal Maven mit in den Build Job aufzunehmen 
(Maven Repository auf dem Build Server) 
- Alternative: generelle Hinterlegung der Dependencies auf dem zentralen Nexus 

(1c) Modifizierung Erweiterung des Java Classpaths via compilerArgs Argument 
- der Java Classpath kann leider nicht direkt gesetzt werden 
- Workaround: Ausgabe Java Classpath in Datei, einlesen, modifizieren und setzen als "compilerArgs" 
- siehe: https://stackoverflow.com/questions/3410548/maven-add-a-folder-or-jar-file-into-current-classpath

(2) Build von Shared Lib mit Java Projekt nur über "ibmint" 
- gemeinsamer Bau der Shared und Java Lib via ibmint (zusätzlicher Eintrag in der pom.xml) 
- initial verprobt 
- issues: Maven Dependencies werden nicht korrekt aufgelöst
- Maven Eintrag: <classpathentry kind="var" path="M2_REPO/org/apache/commons/commons-math3/3.5/commons-math3-3.5.jar"/> 
- Dependencies müssen in classpath als "lib" Eintrag (während dem Build) angepasst werden 
- weiter Verprobung notwendig 
- weiterer Gedanke: Java Projekt als Hauptprojekt --> damit Zugriff auf die pom Dependencies ? 


## Weiterer Punkte 

Ausgabe von aktuellen Classpath: 
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt


ibmint would normally use the .classpath file in the Java project for the classpath when building, and should handle the various different entries in that file these days (early v12 fixpacks might not handle all of them).
It's possible that some entries still aren't being handled properly (would be good to know what's not working) but MQSI_EXTRA_BUILD_CLASSPATH can add additional JARs as a workaround.




