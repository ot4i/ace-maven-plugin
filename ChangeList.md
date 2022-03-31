Status: 28.03.2022 

Changes done: 
- add ibmint package support (as alternative to mqsicreatebar) 
- harmnonized mqsi commands (incl logging) 
- changed config parameter aceTmpDir to fileTmpDir
- consistent file names for bar files 
- made workspace parameter optional 
- updated sample-ace-project  
- updated bar override handling (allows multiple properties, keeps original file) 

Changes planned: 
- maven dependency handling for shared libs 
(unpacking currently in workspace folder - but without further handling)   
- update/rewrite validate bar workspace logic (to ensure that only the required projects are in place) 
- update documentation  
- general cleanup  (.project test file, bin/target folder etc., end-of-lines etc.) 
- update plugin versions / review logback-classic-1.0.13.jar 
- add sample pom for "combined build"

 

Nice to have: 
- optimize CommandExecutionUtil / ProcessOutputCatcher / ProcessOutputLogger 
- include parameter to define if 'tmp files' should be kept 
- incooperate TestApplication changes from Thomas Mattsson (https://github.com/thomas-mattsson)
