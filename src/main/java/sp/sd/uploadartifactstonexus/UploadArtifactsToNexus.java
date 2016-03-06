package sp.sd.uploadartifactstonexus;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpStatus;

import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import org.jenkinsci.remoting.RoleChecker;
import org.jenkinsci.remoting.RoleSensitive;

public class UploadArtifactsToNexus extends Builder implements Serializable{
	private final String protocol;
	private final String nexusUrl;
    private final String nexusUser;
    private final Secret nexusPassword;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
	private final String repository;
	private final String file;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public UploadArtifactsToNexus(String protocol, String nexusUrl, String nexusUser, Secret nexusPassword, String groupId, 
    		String artifactId, String version, String packaging, String repository, String file) {      
		this.protocol = protocol;
		this.nexusUrl = nexusUrl;
		this.nexusUser = nexusUser;
		this.nexusPassword = nexusPassword;
		this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
		this.repository = repository;
		this.file = file;
    }
	
	public String getProtocol() {
		return protocol;
	}

    public String getNexusUrl() {
		return nexusUrl;
	}
	public String getNexusUser() {
		return nexusUser;
	}
	public Secret getNexusPassword() {
		return nexusPassword;
	}
    public String getGroupId() {
		return groupId;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public String getVersion() {
		return version;
	}
	public String getPackaging() {
		return packaging;
	}
	public String getRepository() {
		return repository;
	}
	public String getFile() {
		return file;
	}

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {  
		boolean result = false;
		try
		{
			FilePath artifactFilePath = new FilePath(build.getWorkspace(), TokenMacro.expandAll(build, listener, file));			
			try{
				if(!artifactFilePath.exists())
				{
					listener.getLogger().println(artifactFilePath.getName() + " file doesn't exists");
					return false;
				}
			}catch(Exception e)
			{
				listener.getLogger().println(artifactFilePath.getName() + " file doesn't exists");
				return false;
			}		
						
			try {	
				result = artifactFilePath.act(new ArtifactFileCallable(listener,
							TokenMacro.expandAll(build, listener, nexusUser),
							TokenMacro.expandAll(build, listener, Secret.toString(nexusPassword)),
							TokenMacro.expandAll(build, listener, nexusUrl),
							TokenMacro.expandAll(build, listener, groupId),
							TokenMacro.expandAll(build, listener, artifactId),
							TokenMacro.expandAll(build, listener, version),
							TokenMacro.expandAll(build, listener, repository),
							TokenMacro.expandAll(build, listener,packaging),
							protocol
				));
			}
			catch (Exception e) {
				e.printStackTrace(listener.getLogger());
				return false;
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace(listener.getLogger());
		}		
        return result;
    }
	private static final class ArtifactFileCallable implements FileCallable<Boolean> {
		private static final long serialVersionUID = 1;

		private final BuildListener listener;
		private final String resolvedNexusUser;
		private final String resolvedNexusPassword;
		private final String resolvedNexusUrl;
		private final String resolvedGroupId;
		private final String resolvedArtifactId;
		private final String resolvedVersion;
		private final String resolvedRepository;
		private final String resolvedPackaging;
		private final String resolvedProtocol;

		public ArtifactFileCallable(BuildListener Listener, String ResolvedNexusUser, String ResolvedNexusPassword, String ResolvedNexusUrl, 
										String ResolvedGroupId, String ResolvedArtifactId, String ResolvedVersion, String ResolvedRepository, String ResolvedPackaging, String ResolvedProtocol) {
			this.listener = Listener;
			this.resolvedNexusUser = ResolvedNexusUser;
			this.resolvedNexusPassword = ResolvedNexusPassword;
			this.resolvedNexusUrl = ResolvedNexusUrl;
			this.resolvedGroupId = ResolvedGroupId;
			this.resolvedArtifactId = ResolvedArtifactId;
			this.resolvedVersion = ResolvedVersion;
			this.resolvedRepository = ResolvedRepository;
			this.resolvedPackaging = ResolvedPackaging;
			this.resolvedProtocol = ResolvedProtocol;
		}

		@Override public Boolean invoke(File artifactFile, VirtualChannel channel) {
			
			boolean result = false;
			try(CloseableHttpClient httpClient = HttpClients.createDefault())
			{			
				HttpPost httpPost = new HttpPost(resolvedProtocol + "://" + resolvedNexusUser + ":" + resolvedNexusPassword + "@" + resolvedNexusUrl + "/service/local/artifact/maven/content");
				listener.getLogger().println("GroupId: " + resolvedGroupId);
				listener.getLogger().println("ArtifactId: " + resolvedArtifactId);
				listener.getLogger().println("Version: " + resolvedVersion);
				listener.getLogger().println("File: " + artifactFile.getName());
				listener.getLogger().println("Repository:" + resolvedRepository);
				
				listener.getLogger().println("Uploading artifact " + artifactFile.getName() + " started....");
				FileBody artifactFileBody = new FileBody(artifactFile);
				HttpEntity requestEntity = MultipartEntityBuilder.create()
						.addPart("r", new StringBody(resolvedRepository, ContentType.TEXT_PLAIN))
						.addPart("hasPom", new StringBody("false", ContentType.TEXT_PLAIN))
						.addPart("e", new StringBody(resolvedPackaging, ContentType.TEXT_PLAIN))
						.addPart("g", new StringBody(resolvedGroupId, ContentType.TEXT_PLAIN))
						.addPart("a", new StringBody(resolvedArtifactId, ContentType.TEXT_PLAIN))
						.addPart("v", new StringBody(resolvedVersion, ContentType.TEXT_PLAIN))
						.addPart("p", new StringBody(resolvedPackaging, ContentType.TEXT_PLAIN))
						.addPart("file", artifactFileBody)
						.build();
				httpPost.setEntity(requestEntity);
				try(CloseableHttpResponse response = httpClient.execute(httpPost))
				{
					int statusCode = response.getStatusLine().getStatusCode();
					if(statusCode == HttpStatus.SC_CREATED)
					{
						result = true;
						listener.getLogger().println("Uploading artifact " + artifactFile.getName() + " completed.");
					}
					else
					{
						result = false;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace(listener.getLogger());			
			}
			return result;	
		}
		
		@Override  public void checkRoles(RoleChecker checker) throws SecurityException {
                this.checkRoles(checker);
            }		
	}
	
    
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
    
    @Extension 
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {            
            return true;
        }

        
        public String getDisplayName() {
            return "Upload artifacts to nexus";
        }       

        public FormValidation doCheckNexusUrl(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.error("URL must not be empty");
			}

			if (value.startsWith("http://") && value.startsWith("https://")) {
				return FormValidation.error("URL must not start with http:// or https://");
			}

			try {
				new URL(value).toURI();
			} catch (Exception e) {
				return FormValidation.error(e.getMessage());
			}

			return FormValidation.ok();
		}
		
		public FormValidation doCheckGroupId(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.error("GroupId must not be empty");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckArtifactId(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.error("ArtifactId must not be empty");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckVersion(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.error("Version must not be empty");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckPackaging(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.error("Packaging must not be empty");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckRepository(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.error("Repository must not be empty");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckFile(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.error("File must not be empty");
			}
			return FormValidation.ok();
		}
    }
}

