package ibm.maven.plugins.ace.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import ibm.maven.plugins.ace.generated.maven_pom.Model;


public class MavenUtils {
		
	
	public static List<Artifact> getDependencies(File workspace, List<String> projects,  String scope, Log log) {
		
		List<Artifact> artifactList = new ArrayList<Artifact>();
		
		for (String project : projects) {
			File javaProjectDir = new File(workspace, project);
			File pomFile = new File(javaProjectDir, "pom.xml");
			Model model = null;
			try {
				model = PomXmlUtils.unmarshallPomFile(pomFile);
			} catch (JAXBException e) {
				log.error("could not find pom file: "+e.getMessage()); 
				e.printStackTrace();
			}

			for (ibm.maven.plugins.ace.generated.maven_pom.Dependency dependency : model.getDependencies()
					.getDependency()) {
				log.debug("found dependency: " + dependency);

				if ((dependency.getScope() == null) || (dependency.getScope().equalsIgnoreCase(scope))) {
					String mavenCoordinate = new String(
							dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
					Artifact artifact = new DefaultArtifact(mavenCoordinate);
					artifactList.add(artifact);
				}
			}
		}
		
		return artifactList;
			
	}
	
	/**
	 * resolve - and thus downloads - a list of predefined artifacts 
	 * @param artifacts
	 * @return
	 */
	public static List<File> resolveArtifacts(List<Artifact> artifacts, List<RemoteRepository> remoteRepos, RepositorySystem repoSystem, RepositorySystemSession repoSession, Log log) {
		
		List<File> files = new ArrayList<File>(); 
		log.debug("start resolving artifacts");

		ArtifactRequest request = new ArtifactRequest();
		for (Artifact artifact : artifacts) {
			request.setArtifact(artifact);
			request.setRepositories(remoteRepos);
			ArtifactResult result;

			try {
				result = repoSystem.resolveArtifact(repoSession, request);
				files.add(result.getArtifact().getFile()); 
				log.debug("resolved file: "+result.getArtifact().getFile());
			
	            
			} catch (ArtifactResolutionException e) {
				log.error("issue when resolving artefact: " + e.getMessage());
			}

		}
		
		return files; 
		
	}
	
	
	

}
