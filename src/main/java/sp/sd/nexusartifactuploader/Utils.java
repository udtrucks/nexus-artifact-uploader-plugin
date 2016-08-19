package sp.sd.nexusartifactuploader;

import com.google.common.base.Strings;
import hudson.model.TaskListener;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by suresh on 5/20/2016.
 */
public final class Utils {
    private Utils() {
    }

    public static Boolean uploadArtifact(File artifactFile, TaskListener Listener, String ResolvedNexusUser,
                                 String ResolvedNexusPassword, String ResolvedNexusUrl,
                                 String ResolvedGroupId, String ResolvedArtifactId, String ResolvedVersion,
                                 String ResolvedRepository, String ResolvedPackaging, String ResolvedType, String ResolvedClassifier,
                                 String ResolvedProtocol) throws IOException {
        Boolean result = false;
        if (Strings.isNullOrEmpty(ResolvedNexusUrl)) {
            Listener.getLogger().println("Url of the Nexus is empty. Please enter Nexus Url.");
            return false;
        }
        try {
            URI Url = new URI(ResolvedProtocol + "://" + ResolvedNexusUrl + "/service/local/artifact/maven/content");
            HttpHost host = new HttpHost(Url.getHost(), Url.getPort(), Url.getScheme());
            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(Url.getHost(), Url.getPort()), new UsernamePasswordCredentials(ResolvedNexusUser, ResolvedNexusPassword));
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(host, basicAuth);

            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);
            CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            HttpPost httpPost = new HttpPost(ResolvedProtocol + "://" + ResolvedNexusUrl + "/service/local/artifact/maven/content");
            Listener.getLogger().println("GroupId: " + ResolvedGroupId);
            Listener.getLogger().println("ArtifactId: " + ResolvedArtifactId);
            Listener.getLogger().println("Classifier: " + ResolvedClassifier);
            Listener.getLogger().println("Type: " + ResolvedType);
            Listener.getLogger().println("Version: " + ResolvedVersion);
            Listener.getLogger().println("File: " + artifactFile.getName());
            Listener.getLogger().println("Repository:" + ResolvedRepository);

            Listener.getLogger().println("Uploading artifact " + artifactFile.getName() + " started....");
            FileBody artifactFileBody = new FileBody(artifactFile);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addPart("r", new StringBody(ResolvedRepository, ContentType.TEXT_PLAIN))
                    .addPart("hasPom", new StringBody("false", ContentType.TEXT_PLAIN))
                    .addPart("g", new StringBody(ResolvedGroupId, ContentType.TEXT_PLAIN))
                    .addPart("a", new StringBody(ResolvedArtifactId, ContentType.TEXT_PLAIN))
                    .addPart("v", new StringBody(ResolvedVersion, ContentType.TEXT_PLAIN))
                    .addPart("p", new StringBody(ResolvedPackaging, ContentType.TEXT_PLAIN));
            if (ResolvedType != null && ResolvedType.length() > 0) { 
                builder.addPart("e", new StringBody(ResolvedType, ContentType.TEXT_PLAIN));
            } else {
                builder.addPart("e", new StringBody(ResolvedPackaging, ContentType.TEXT_PLAIN)); // for backward compatibility
            } 
            if (ResolvedClassifier != null && ResolvedClassifier.length() > 0) {
                builder.addPart("c", new StringBody(ResolvedClassifier, ContentType.TEXT_PLAIN));
            }
            builder.addPart("file", artifactFileBody);
            HttpEntity requestEntity = builder.build();
            httpPost.setEntity(requestEntity);
            try (CloseableHttpResponse response = httpClient.execute(host, httpPost, localContext)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_CREATED) {
                    result = true;
                    Listener.getLogger().println("Uploading artifact " + artifactFile.getName() + " completed.");
                } else {
                    Listener.getLogger().println("Reason Phrase: " + response.getStatusLine().getReasonPhrase());
                    HttpEntity entity = response.getEntity();
                    String content = EntityUtils.toString(entity);
                    Listener.getLogger().println(content);
                    result = false;
                }
            }
        } catch (URISyntaxException e) {
            Listener.getLogger().println(e.getMessage());
            result = false;
        }
        return result;
    }
}
