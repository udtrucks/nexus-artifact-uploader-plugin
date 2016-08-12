package sp.sd.nexusartifactuploader.steps;

/**
 * Created by suresh on 5/19/2016.
 */

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.base.Strings;
import hudson.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.MasterToSlaveFileCallable;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import sp.sd.nexusartifactuploader.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public final class NexusArtifactUploaderStep extends AbstractStepImpl {
    private final String protocol;
    private final String nexusUrl;
    private final String nexusUser;
    private final String nexusPassword;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
    private final String type;
    private final String classifier;
    private final String repository;
    private final String file;
    private final
    @CheckForNull
    String credentialsId;

    @DataBoundConstructor
    public NexusArtifactUploaderStep(String protocol, String nexusUrl, String nexusUser, String nexusPassword, String groupId,
                                     String artifactId, String version, String packaging, String type, String classifier,
                                     String repository, String file, String credentialsId) {
        this.protocol = protocol;
        this.nexusUrl = nexusUrl;
        this.nexusUser = nexusUser;
        this.nexusPassword = Secret.fromString(nexusPassword).getEncryptedValue();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
        this.type = type;
        this.classifier = classifier;
        this.repository = repository;
        this.file = file;
        this.credentialsId = credentialsId;
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

    public String getNexusPassword() {
        return Secret.decrypt(nexusPassword).getPlainText();
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

    public String getType() {
        return type;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getRepository() {
        return repository;
    }

    public String getFile() {
        return file;
    }

    public
    @Nullable
    String getCredentialsId() {
        return credentialsId;
    }

    public StandardUsernameCredentials getCredentials() {
        StandardUsernameCredentials credentials = null;
        try {

            credentials = credentialsId == null ? null : this.lookupSystemCredentials(credentialsId);
            if (credentials != null) {
                return credentials;
            }
        } catch (Throwable t) {

        }

        return credentials;
    }

    public static StandardUsernameCredentials lookupSystemCredentials(String credentialsId) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider
                        .lookupCredentials(StandardUsernameCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(credentialsId)
        );
    }

    public String getUsername(EnvVars environment) {
        String Username = null;
        if (Strings.isNullOrEmpty(nexusUser)) {
            Username = "";
        } else {
            Username = environment.expand(nexusUser);
        }
        if (!Strings.isNullOrEmpty(credentialsId)) {
            Username = this.getCredentials().getUsername();
        }
        return Username;
    }

    public String getPassword(EnvVars environment) {
        String Password = null;
        if (nexusPassword == null) {
            Password = "";
        } else {
            Password = environment.expand(Secret.decrypt(nexusPassword).getPlainText());
        }
        if (!Strings.isNullOrEmpty(credentialsId)) {
            Password = Secret.toString(StandardUsernamePasswordCredentials.class.cast(this.getCredentials()).getPassword());
        }
        return Password;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(Execution.class);
        }

        @Override
        public String getFunctionName() {
            return "nexusArtifactUploader";
        }

        @Override
        public String getDisplayName() {
            return "Nexus non-maven artifacts uploader";
        }

        public FormValidation doCheckNexusUrl(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("URL must not be empty");
            }

            if (value.startsWith("http://") || value.startsWith("https://")) {
                return FormValidation.error("URL must not start with http:// or https://");
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

        public FormValidation doCheckType(@QueryParameter String value) {
            return FormValidation.ok();
        }

        public FormValidation doCheckClassifier(@QueryParameter String value) {
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

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item owner) {
            if (owner == null || !owner.hasPermission(Item.CONFIGURE)) {
                return new ListBoxModel();
            }
            return new StandardUsernameListBoxModel().withEmptySelection().withAll(CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, owner, ACL.SYSTEM, Collections.<DomainRequirement>emptyList()));
        }
    }

    public static final class Execution extends AbstractSynchronousNonBlockingStepExecution<Boolean> {
        @Inject
        private transient NexusArtifactUploaderStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient FilePath ws;

        @StepContextParameter
        private transient Run build;

        @StepContextParameter
        private transient Launcher launcher;

        @Override
        protected Boolean run() throws Exception {
            Boolean result = false;
            EnvVars envVars = build.getEnvironment(listener);
            FilePath artifactFilePath = new FilePath(ws, build.getEnvironment(listener).expand(step.getFile()));
            if (!artifactFilePath.exists()) {
                listener.getLogger().println(artifactFilePath.getName() + " file doesn't exists");
                throw new IOException(artifactFilePath.getName() + " file doesn't exists");
            }
            else {
                result = artifactFilePath.act(new ArtifactFileCallable(listener,
                        step.getUsername(envVars),
                        step.getPassword(envVars),
                        envVars.expand(step.getNexusUrl()),
                        envVars.expand(step.getGroupId()),
                        envVars.expand(step.getArtifactId()),
                        envVars.expand(step.getVersion()),
                        envVars.expand(step.getRepository()),
                        envVars.expand(step.getPackaging()),
                        envVars.expand(step.getType()),
                        envVars.expand(step.getClassifier()),
                        step.getProtocol()
                ));
            }
            return result;
        }
        private static final long serialVersionUID = 1L;
    }

    private static final class ArtifactFileCallable extends MasterToSlaveFileCallable<Boolean> {

        private final TaskListener listener;
        private final String resolvedNexusUser;
        private final String resolvedNexusPassword;
        private final String resolvedNexusUrl;
        private final String resolvedGroupId;
        private final String resolvedArtifactId;
        private final String resolvedVersion;
        private final String resolvedRepository;
        private final String resolvedPackaging;
        private final String resolvedType;
        private final String resolvedClassifier;
        private final String resolvedProtocol;

        public ArtifactFileCallable(TaskListener Listener, String ResolvedNexusUser, String ResolvedNexusPassword,
                                    String ResolvedNexusUrl, String ResolvedGroupId, String ResolvedArtifactId,
                                    String ResolvedVersion, String ResolvedRepository, String ResolvedPackaging,
                                    String ResolvedType, String ResolvedClassifier, String ResolvedProtocol) {
            this.listener = Listener;
            this.resolvedNexusUser = ResolvedNexusUser;
            this.resolvedNexusPassword = ResolvedNexusPassword;
            this.resolvedNexusUrl = ResolvedNexusUrl;
            this.resolvedGroupId = ResolvedGroupId;
            this.resolvedArtifactId = ResolvedArtifactId;
            this.resolvedVersion = ResolvedVersion;
            this.resolvedRepository = ResolvedRepository;
            this.resolvedPackaging = ResolvedPackaging;
            this.resolvedType = ResolvedType;
            this.resolvedClassifier = ResolvedClassifier;
            this.resolvedProtocol = ResolvedProtocol;
        }

        @Override
        public Boolean invoke(File artifactFile, VirtualChannel channel) throws IOException {
            return Utils.uploadArtifact(artifactFile, listener, resolvedNexusUser, resolvedNexusPassword, resolvedNexusUrl,
                    resolvedGroupId, resolvedArtifactId, resolvedVersion, resolvedRepository, resolvedPackaging, 
                    resolvedType, resolvedClassifier, resolvedProtocol);
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {

        }
    }
}
