# nexus-artifact-uploader

This plugin goal is to upload artifacts generated from non-maven projects to Sonatype Nexus.
Used the REST API of the Nexus to upload a multi-part form POST to /service/local/artifact/maven/content.

Here are all the available form parameters can be passed

r : repository

e - extension

g - group id

a - artifact id

v - version

p - packaging

c - classifier (optional)

file - each file to be uploaded, use one file parameter per file.

# Job DSL example

    freeStyleJob('NexusArtifactUploaderJob') {
        steps {
          nexusArtifactUploader {
            protocol('http')
            nexusUrl('localhost:8080/nexus')
            groupId('sp.sd')
            artifactId('nexus-artifact-uploader')
            version('2.4')
            packaging('hpi')
            type('jar')
            classifier('debug')
            repository('NexusArtifactUploader')
            file('nexus-artifact-uploader.hpi')
            credentialsId('44620c50-1589-4617-a677-7563985e46e1')
          }
        }
    }
