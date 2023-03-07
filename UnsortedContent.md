# TODO: 

- sample: mqsicreate --> false for ibmint 
- check if toolkit contains maven by default 
- source code packaging --> samples 


### 5) Using the plugin
If your run on Linux you have to install the xvfb package and open a display. Run e.g. the following commands on unbuntu (make sure to select the correct bashrc file depending on the 'build user'): 

- `sudo apt-get install -y xvfb`
-  echo "Xvfb -ac :100 &" >> /root/.bashrc
-  echo "export DISPLAY=:100" >> /root/.bashrc

In the case of RHEL you have to install further packages. Follow the instructions here: https://www.ibm.com/support/pages/node/6823669


### 6) Specific notes on plugin configuration 

**(1) Use of javaclass loaders**   
A Java Compute Node allows you to set a specific "class loader"  within the Node properties section.
Typically this will be a "Shared Lib Classloader". See for example: https://www.ibm.com/docs/en/integration-bus/10.0?topic=libraries-shared-java-files 
  
If you use this construct you have to set the plugin parameter `useClassloaders` to true. 
Otherwise the build will fail. For the syntax take a look at the sample for the pom files. 


===========

--> FÜR die SampleApp README.md !! 


# Known Limitations  
- As of today only the ace-maven-plugin only supports the dependency handling / unpacking of SharedLibs. 
