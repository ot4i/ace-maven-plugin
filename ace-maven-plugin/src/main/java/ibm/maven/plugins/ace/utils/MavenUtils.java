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
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;

import ibm.maven.plugins.ace.generated.maven_pom.Model;

public class MavenUtils {

	public static List<Artifact> getDependencies(File workspace, List<String> projects, String scope, Log log) {

		List<Artifact> artifactList = new ArrayList<Artifact>();

		for (String project : projects) {
			File javaProjectDir = new File(workspace, project);
			File pomFile = new File(javaProjectDir, "pom.xml");
			Model model = null;

			try {
				model = PomXmlUtils.unmarshallPomFile(pomFile);
			} catch (JAXBException e) {
				log.warn("could not find pom file; returning empty dependency list");
				return artifactList;
			}

			// validate that model contains dependency entries
			if (model.getDependencies() == null) {
				log.warn("pom file does not include any dependency definition; returning empty dependency list");
				return artifactList;
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
	 * 
	 * @param artifacts
	 * @return
	 */
	public static List<File> resolveArtifacts(List<Artifact> artifacts, List<RemoteRepository> remoteRepos,
			RepositorySystem repoSystem, RepositorySystemSession repoSession, Log log) {

		List<File> files = new ArrayList<File>();
		log.debug("start resolving artifacts");

		ArtifactRequest request = new ArtifactRequest();

		// create a CollectRequest - for all first level dependencies 
		CollectRequest collectRequest = new CollectRequest();

		for (Artifact artifact : artifacts) {
			request.setArtifact(artifact);
			request.setRepositories(remoteRepos);

			collectRequest.addDependency(new Dependency(artifact, "compile"));
		}

		DependencyNode node;
		
		try {
			
			//get a dependency node (graph) based on the defined (first level dependencies) 
			node = repoSystem.collectDependencies(repoSession, collectRequest).getRoot();

			//resolve the dependency node (graph) 
			DependencyRequest dependencyRequest = new DependencyRequest();
			dependencyRequest.setRoot(node);
			DependencyResult depResults = repoSystem.resolveDependencies(repoSession, dependencyRequest);

			for (ArtifactResult artifactResult : depResults.getArtifactResults()) {
				files.add(artifactResult.getArtifact().getFile());
				log.debug("resolved file: " + artifactResult.getArtifact().getFile());
			}

		} catch (DependencyCollectionException e) {
			log.error("run into DependencyCollectionException: " + e);
		} catch (DependencyResolutionException e) {
			log.error("run into DependencyResolutionException: " + e);
		}

		return files;

	}
}
