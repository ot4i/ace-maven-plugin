# Basic Setup  

**Step 1: download the toolkit tar file** 
download and store the toolkit tar file as described in  https://www.ibm.com/docs/en/app-connect/12.0?topic=enterprise-download-ace-developer-edition-get-started

**Step 2: Unpack the tar file to a directory**
tar -xzvf ace-12.0.n.0.tar.gz
note: all users who need to work with the ace / mqsi commands needs access to the folder 

**Step 3: Accept license - globally to ensure it's working for all users** 
./ace make registry global accept license 

**Step 4: add 'working user' and set password and group**
useradd jenkins
passwd jenkins 
usermod -a -G mqbrkrs jenkins 

**Step 5: verify that user has access to mqsiprofile an can use it** 
su jenkins 
ls -la <installdir>/ace-12.0.n.0/server/bin/mqsiprofile   --> should allow to list the files; if acess denied is shown the <installdir> is not setup correctly 
. installdir>/ace-12.0.n.0/server/bin/mqsiprofile --> to check if mqsiprofile could be "sourced" 

# Run a "test build" to verify the setup 
Prerequsites: mvn is installed (yum install maven) 

**Step 2: start a new session for your user (e.g. jenkins)**        
Seems that there is an issue when mqsiprofile was already sourced before the build (see step 5 above) 
Currently investigating it. 
Workaround: create a new session for the user
  
**Step 3: clone the git repro**
- change to a directory of your choise (e.g. /tmp) 
- clone the Git Repo:  git clone https://github.com/ChrWeissDe/ace-maven-plugin.git 

**Step 4: build the ace-maven-plugin** 
- change to directory ace-maven-plugin/ace-maven-plugin 
- run 'mvn clean install'  --> this will build the plugin and deploy it to the local maven repository 
(important: each user has an own local maven repo; thus either configure a general repo or make sure to run it for the required users) 

**Step 5: prepare the sample-java-project**
- change to /ace-maven-plugin/sample-java-project
- modify /ace-maven-plugin/sample-java-project/LargeMessages/pom.xml and set the correct path for <toolkitInstallDir> and <aceRunDir>

Example: 
```
vi ./LargeMessages/pom.xml 

....
  
<!-- installation directories for toolkit and server (runtime) -->
<toolkitInstallDir>/opt/ace-12.0.6.0/tools</toolkitInstallDir>
<aceRunDir>/opt/ace-12.0.6.0/server/bin</aceRunDir>  
```
  
**Step 6: run the sample-java-project**
start a xvfb terminal in the background:  Xvfb -ac :101 & 
export the display:  export DISPLAY=:101
run in  /ace-maven-plugin/sample-java-project: --> mvn -f ./combine-java-aceapp-pom.xml clean install

# Install and configure Jenkins
Prerequisite: 
- ensure that maven is installed on your system 
  
The official Jenkins install docu can be found here: https://www.jenkins.io/doc/book/installing/linux/ 
However the steps below vary a little bit as we install an 'older LTS' release - as we only require Java 8

//ensure to have wget and git available
sudo yum install wget git -y  

//configure jenkins repository; important: using redhat mirror because of key issues on jenkins 
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat/jenkins.repo 
sudo rpm --import https://pkg.jenkins.io/redhat/jenkins.io.key
  
//install jenkins
sudo yum install jenkins-2.346-1.1 //please check the lates version 
  
//enabling and starting the jenkins service
sudo systemctl enable jenkins
sudo systemctl start jenkins
  
important: default user for jenkins is "jenkins"
This could be change in  cat /etc/sysconfig/jenkins (look for entry 'JENKINS_USER') 

follow the steps on https://www.jenkins.io/doc/book/installing/linux/ to 
- change the port (default 8080) 
- adjust firewall settings 
- unlock Jenkins (requires a "one time password stored in the installed files) 
- install the suggested plugins 
  
  

 

