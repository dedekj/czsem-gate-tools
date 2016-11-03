package czsem.maven.customproperties;

import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @goal setproperties
 * @requiresDependencyResolution compile+runtime
 * @see http://maven.apache.org/developers/mojo-api-specification.html - for complete list of supported mojo java annotations 
 */
public class MainMojo extends AbstractMojo {

	/** @parameter default-value="${project}" */
	private MavenProject mavenProject;
	
	@Override
	public void execute() throws MojoExecutionException {
		Properties props = mavenProject.getProperties();
		for (Object objArtifact : mavenProject.getArtifacts())
		{
			Artifact artifact = (Artifact) objArtifact;

			String key = String.format("dependency.%s.%s.version", artifact.getGroupId(), artifact.getArtifactId());
			props.put(key, artifact.getVersion());
			getLog().info(String.format("setting artifact version '%s' to property: %s", artifact.getBaseVersion(), key));
		}
	}
}
