## ibm int 

Aktuelle unterstützte Use Cases: 
- Shared Lib mit Java Anteil - Java Part nur mit Maven Dependencies     
- Application, REST APIs (ohne Java Anteil / Java Calls) 
- Polify Project 


## Handling Java Classpath bei Shared Libraries   

Ausgangslage: 
Shared Library mit einem verlinkten Java Projekt.   
Das Java Projekt beinhaltet Maven Dependencies und Abhängigkeite zu den "App Connec Enterprise Standard Klaassen" (bspws. jplugin.jar und javacompute.jar aus <ace-install-dir>/server/classes). 

Ziel:    
Bau der  "Shared Library" als bar Datei.     

Problem: unterschiedliche Verhaltensweise bei mqsicreatebar und ibmint. 

Bei mqsicreate wird über die Eclipse Umgebung **beide Abhängigkeiten** korrekt aufgelöst, und die bar Datei korrekt gebaut 
Bei ibmint werden die Maven Dependencies nicht aufgelöst, damit schlägt die Kompilierung fehl. 

Optionen:  
== Kombibuild Standard Maven Build Java Projekt + ace-maven-plugin mit ibmint für SharedLib 

(1) Zusätzliche als Maven Dependencies - Variante 1 - Zentrales Nexus 
- Hinterlegung der jars in Artifactory
- Definierung der Maven Dependency mit  "compile scope"  
- unverprobt 
- Bewertung: technisch umsetzbar, zusätzlicher Aufwand für das Handling / Management der jars / Dependencies 

(2) Zusätzliche als Maven Dependencies - Variante 2 - Lokales Repository 
- Analog zu Variante - Hinterlegung der jar Datei seperat im "lokalen Repository" (via maven-install-plugin) 
- Referenzierung über die Dependency - zum Beispiel via Scope "provided" 
- Bewertung: technisch umsetzbar, zusätzlicher Aufwand für das Handling / Management der jars / Dependencies 


(3) Zusätzlich als Maven Dependency: - Variante 3 - System Scope 
- Hinterlegung der jars als "System Scope" mit Angabe vom "System Path" 
siehe auch: https://stackoverflow.com/questions/2479046/maven-how-to-add-additional-libs-not-available-in-repo
- **verprobt, funktioniert** 
- wird generell als "bad practice" angesehen, da es vor allem beim Erstellung von "Maven Assemblies" als Bad Practice angesehen wird; 
zu prüfen, ob wird das in unserem Fall vernachlässigen können. 
- aktuell als "deprecated" gemarkt, unklar aber wann es wirklich deprecated wird 

(4) Kopieren der zusätzlichen Klassen nach <java-install-dir>/lib/ext 
- **verprobt, funktioniert** 
- generell aber "bad practice" - da keine direkte / explizite Kontrolle über die Dateien 
- weiterhin ist /lib/ext für zukünftige Java Installationen deprecated / abgekündigt 

(5) Modifizierung Erweiterung des Java Classpaths 
- der Java Classpath kann leider nicht direkt gesetzt werden 
- Workaround: Ausgabe Java Classpath in Datei, Einlesen, Modifiezieren und Setzen als "CompilerArgs" 
- siehe: 


== nur ace-maven-plugin für SharedLib  
(6) ibmint 
- gemeinsamer Bau der Shared und Java Lib via ibmint (zusätzlicher Eintrag in der pom.xml) 
- initial verprobt 
- issues: Maven Dependencies werden nicht korrekt aufgelöst
- Maven Eintrag: <classpathentry kind="var" path="M2_REPO/org/apache/commons/commons-math3/3.5/commons-math3-3.5.jar"/> 
- müßte entsprechend angepasst werden auf "lib" 
- weiter Verprobung notwendig 
- Issue: kein Zugriff auf Java Projekt pom - wenn über die SharedLib gebaut wird? 
- weiterer Gedanke: Java Projekt als Hauptprojekt --> damit Zugriff auf die pom Dependencies 


## Weiterer Punkte 

Ausgabe von aktuellen Classpath: 
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt


ibmint would normally use the .classpath file in the Java project for the classpath when building, and should handle the various different entries in that file these days (early v12 fixpacks might not handle all of them).
It's possible that some entries still aren't being handled properly (would be good to know what's not working) but MQSI_EXTRA_BUILD_CLASSPATH can add additional JARs as a workaround.




