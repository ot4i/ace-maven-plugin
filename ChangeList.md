Status: 18.03.2022 

Changes done: 
- add ibmint package support (as alternative to mqsicreatebar) 
- align handling of executing mqsi commands (incl logging) 
- changed aceTmpDir to fileTmpDir

Changes planned: 
- maven dependency handling for shared libs 
(unpacking currently in workspace folder - but without further handling) 
- consistent file names for resulting bar files 
- make workspace paraemter optional 
- add sample project 
- update documentation 
 

Nice to have: 
- optimize CommandExecutionUtil / ProcessOutputCatcher / ProcessOutputLogger 
- incooperate TestApplication changes from Thomas Mattsson (https://github.com/thomas-mattsson)
