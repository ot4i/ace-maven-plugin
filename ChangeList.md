Status: 01.04.2022 

Changes done: 
- added ibmint package support (as alternative to mqsicreatebar) 
- harmonized  mqsi commands (incl logging) 
- changed config parameter aceTmpDir to fileTmpDir
- added further default for properties 
- consistent file names for bar files 
- made workspace parameter optional 
- updated sample-ace-project / incl. sample pom for a combined java / shared lib build 
- updated bar override handling (allows multiple properties, keeps original file)
- added maven dependencies handling of shared libs 
- cleanup used maven dependencies / libraries - incl. update to latests possible versions

Changes planned: 
- update/rewrite validate bar workspace logic (to ensure that only the required projects are in place)
- further cleanup pom  
- mqsicreatebar / unpackage 
- update documentation  
- further cleanup  (.project test file, bin/target folder etc., end-of-lines etc.) 


Nice to have: 
- optimize CommandExecutionUtil / ProcessOutputCatcher / ProcessOutputLogger 
- include parameter to define if 'tmp files' should be kept 
- incooperate TestApplication changes from Thomas Mattsson (https://github.com/thomas-mattsson)
