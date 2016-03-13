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

file - each file to be uploaded, use one file parameter per file.
