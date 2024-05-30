package com.github.onsdigital.dp.uploadservice.api;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.NameValuePair;

import com.github.onsdigital.dp.uploadservice.api.exceptions.ConnectionException;

public class APIClient implements Client  {

    private String hostname;
    private String authToken;
    private CloseableHttpClient httpClient;

    public APIClient(String hostname, String authToken) {
        this.hostname = hostname;
        this.authToken = authToken;
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void uploadFile(File file, List<NameValuePair> params) {
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder("http://dp-upload-service:25100/upload-new");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        uriBuilder.addParameters(params);

        CloseableHttpResponse httpResponse;
        try {
            HttpPost request = new HttpPost(uriBuilder.build());
            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("bob", file);
            builder.setBoundary("--TFJ5T8Nl2Py-S_BZXD5_FaEzCCuRXVXL0--[\\r][\\n]");
            final HttpEntity entityReq = builder.build();
            request.setEntity(entityReq);
            request.addHeader("Authorization", "Bearer " + authToken);
            httpResponse = httpClient.execute(request);

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    System.out.println("THE RESPONSE IS");
                    System.out.println(httpResponse);
                    System.out.println(instream.read());
                }
            }
        } catch (Exception e) {
            throw new ConnectionException("error talking to upload service", e);
        }
    }

    private String removeTrailingSlash(String url){
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url; // Return unchanged string if the string does not end with a slash
    }
}