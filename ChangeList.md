Status: 18.03.2022 

Changes done: 
- add ibmint package support (as alternative to mqsicreatebar) 
- harmnonized mqsi commands (incl logging) 
- changed config parameter aceTmpDir to fileTmpDir
- consistent file names for bar files 
- made workspace parameter optional  

Changes planned: 
- maven dependency handling for shared libs 
(unpacking currently in workspace folder - but without further handling) 
- rewrite validate bar workspace logic (to ensure that only the required projects are in place) 
- add sample project 
- update documentation 
 

Nice to have: 
- optimize CommandExecutionUtil / ProcessOutputCatcher / ProcessOutputLogger 
- include parameter to define if 'tmp files' should be kept 
- incooperate TestApplication changes from Thomas Mattsson (https://github.com/thomas-mattsson)
