package sp.sd.nexusartifactuploader.dsl;

import javaposse.jobdsl.dsl.Context;

/**
 * Created by suresh on 9/29/2016.
 */
public class NexusArtifactUploaderJobDslContext implements Context {
    String nexusVersion;
    String protocol;
    String nexusUrl;
    String groupId;
    String artifactId;
    String version;
    String type;
    String classifier;
    String repository;
    String file;
    String credentialsId;

    void nexusVersion(String nexusVersion) {
        this.nexusVersion = nexusVersion;
    }

    void protocol(String protocol) {
        this.protocol = protocol;
    }

    void nexusUrl(String nexusUrl) {
        this.nexusUrl = nexusUrl;
    }

    void groupId(String groupId) {
        this.groupId = groupId;
    }

    void artifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    void version(String version) {
        this.version = version;
    }

    void type(String type) {
        this.type = type;
    }

    void classifier(String classifier) {
        this.classifier = classifier;
    }

    void repository(String repository) {
        this.repository = repository;
    }

    void file(String file) {
        this.file = file;
    }

    void credentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
}
