Status: 01.04.2022 

Changes done: 
- added ibmint package support (as alternative to mqsicreatebar) 
- harmnonized mqsi commands (incl logging) 
- changed config parameter aceTmpDir to fileTmpDir
- consistent file names for bar files 
- made workspace parameter optional 
- updated sample-ace-project  
- updated bar override handling (allows multiple properties, keeps original file)
- added maven dependencies handling of shared libs 
- cleanup used maven dependencies / libraries - incl. update to latests possible versions

Changes planned: 
- update/rewrite validate bar workspace logic (to ensure that only the required projects are in place) 
- update documentation  
- add sample pom for "combined build" (sample project: Java_LIB and Calculator_LIB) 
- further cleanup  (.project test file, bin/target folder etc., end-of-lines etc.) 


Nice to have: 
- optimize CommandExecutionUtil / ProcessOutputCatcher / ProcessOutputLogger 
- include parameter to define if 'tmp files' should be kept 
- incooperate TestApplication changes from Thomas Mattsson (https://github.com/thomas-mattsson)
