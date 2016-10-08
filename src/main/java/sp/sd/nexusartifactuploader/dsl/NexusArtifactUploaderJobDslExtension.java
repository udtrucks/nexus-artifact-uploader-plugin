package sp.sd.nexusartifactuploader.dsl;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import sp.sd.nexusartifactuploader.NexusArtifactUploader;

/**
 * Created by suresh on 9/29/2016.
 */

/*
```
 For example:
 ```
    freeStyleJob('NexusArtifactUploaderJob') {
        steps {
          nexusArtifactUploader {
            nexusVersion('nexus2')
            protocol('http')
            nexusUrl('localhost:8080/nexus')
            groupId('sp.sd')
            artifactId('nexus-artifact-uploader')
            version('2.4')
            type('jar')
            classifier('debug')
            repository('NexusArtifactUploader')
            file('nexus-artifact-uploader.jar')
            credentialsId('44620c50-1589-4617-a677-7563985e46e1')
          }
        }
    }
*/

@Extension(optional = true)
public class NexusArtifactUploaderJobDslExtension extends ContextExtensionPoint {

    @RequiresPlugin(id = "nexus-artifact-uploader", minimumVersion = "2.6")
    @DslExtensionMethod(context = StepContext.class)
    public Object nexusArtifactUploader(Runnable closure) {
        NexusArtifactUploaderJobDslContext context = new NexusArtifactUploaderJobDslContext();
        executeInContext(closure, context);

        return new NexusArtifactUploader(context.nexusVersion, context.protocol, context.nexusUrl, null, null, context.groupId, context.artifactId, context.version, context.type, context.classifier, context.repository, context.file, context.credentialsId);
    }
}
