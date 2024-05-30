package com.github.onsdigital.dp.uploadservice.api;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

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
    public void uploadFile(String fileName) {
        CloseableHttpResponse httpResponse;

        try {
            HttpPost request = new HttpPost(removeTrailingSlash(hostname) + "/upload-new/");
            request.addHeader("Authorization", "Bearer " + authToken);
            httpResponse = httpClient.execute(request);
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