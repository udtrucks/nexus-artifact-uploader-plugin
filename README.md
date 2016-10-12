# nexus-artifact-uploader

This plugin goal is to upload artifacts generated from non-maven projects to Nexus

This plugin now supports Nexus-2.x & Nexus-3.x.

Uploading snapshots is not supported by this plugin.

# Job DSL example

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
