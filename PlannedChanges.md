Status: 18.03.2022 

Done changes: 
(1) Add ibmint package support as alternative to mqsicreatebar 

List of planned changes for the plugin: 
(1) Fix unpacking of "SharedLibs" defined as "maven dependency" within a main integraton (Application, Rest API)  
- unpack is currently done in "workspace" directory; however this is not sufficient; for mqsicreatebar a valid Eclipse project must be created 
(2) Log all mqsi / ibm int commands 
- for debugging purpose 
(3) incoperate changes from https://github.com/thomas-mattsson 
- includes a central "execute command class" - thus a good candidate to standardize also issue 2 
(4) Make workspace parameter optional 
- should be not required - workspace is typically the parent folder of the "maven project run" 
(5) Ensure consistent file names for bar files 
- at the moment it looks like that the bar names are different when running no applybaroverride or applybaroverride 
(6) Include new sample project 
(7) Include test-project handling (also already available in one of the GitHub forks) 
