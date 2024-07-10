package com.github.onsdigital.dp.uploadservice.api;

import com.github.onsdigital.dp.uploadservice.api.configuration.Configuration;
import com.github.onsdigital.dp.uploadservice.api.exceptions.ConnectionException;
import com.github.onsdigital.dp.uploadservice.api.exceptions.UploadChunkException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

public class APIClient implements Client  {
    private String hostname;
    private String authToken;
    private CloseableHttpClient httpClient;
    private Configuration configuration;

    public APIClient(String hostname, String authToken) {
        this.hostname = hostname;
        this.authToken = authToken;
        this.httpClient = HttpClients.createDefault();
        this.configuration = new Configuration();
    }

    @Override
    public void uploadResumableFile(File file, List<NameValuePair> params) throws IOException {
        long fileSize = file.length();
        int totalChunks = (int) Math.ceil((double) fileSize / this.configuration.getChunkSize());

        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            params.add(new BasicNameValuePair("resumableTotalSize", String.valueOf(fileSize))); // the total size of the file
            params.add(new BasicNameValuePair("resumableTotalChunks", String.valueOf(totalChunks)));

            for (int chunkNumber = 0; chunkNumber < totalChunks; chunkNumber++) {
                int start = chunkNumber * this.configuration.getChunkSize();
                int resumableChunkSize = Math.min(this.configuration.getChunkSize(), (int) (fileSize - start));
                byte[] buffer = new byte[resumableChunkSize];

                int bytesRead = fileInputStream.read(buffer, 0, resumableChunkSize);
                if (bytesRead == -1) break;

                params.add(new BasicNameValuePair("resumableChunkNumber", String.valueOf(chunkNumber+1)));
                params.add(new BasicNameValuePair("resumableChunkSize", String.valueOf(resumableChunkSize)));

                sendChunk(buffer, chunkNumber, totalChunks, file.getName(), params);

                params.removeIf(nameValuePair -> nameValuePair.getName().equals("resumableChunkNumber"));
                params.removeIf(nameValuePair -> nameValuePair.getName().equals("resumableChunkSize"));
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void sendChunk(byte[] chunk, int chunkNumber, int totalChunks, String fileName, List<NameValuePair> params) throws IOException {
        URIBuilder uriBuilder;
        try {
            String endPoint = removeTrailingSlash(hostname) + "/upload-new";
            uriBuilder = new URIBuilder(endPoint);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        uriBuilder.addParameters(params);

        try {
            HttpPost request = new HttpPost(uriBuilder.build());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", chunk, ContentType.APPLICATION_OCTET_STREAM, fileName);

            request.setEntity(builder.build());
            request.addHeader("Authorization", "Bearer " + authToken);

            HttpClientResponseHandler<String> responseHandler = classicHttpResponse -> {
                int responseCode = classicHttpResponse.getCode();
                HttpEntity entity = classicHttpResponse.getEntity();

                if (responseCode == 200) {
                    info().log("Chunk " + chunkNumber + " uploaded successfully.");
                } else if (responseCode == 201) {
                    info().log("Chunk " + chunkNumber + " uploaded successfully. All chunks are now uploaded (" + totalChunks + ").");
                } else {
                    info().log("Failed to upload chunk #" + chunkNumber + ". Response code: " + responseCode);
                    throw new UploadChunkException("error uploading chunk" + chunkNumber);
                }

                if (entity != null) {
                    return EntityUtils.toString(entity);
                }
                return null;
            };

            httpClient.execute(request, responseHandler);
        } catch (Exception e) {
            throw new ConnectionException("error talking to upload service", e);
        }
    }

    protected String removeTrailingSlash(String url){
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url; // Return unchanged string if the string does not end with a slash
    }
}