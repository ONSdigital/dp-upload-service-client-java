package com.github.onsdigital.dp.uploadservice.api;

import com.github.onsdigital.dp.uploadservice.api.exceptions.ConnectionException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

public class APIClient implements Client  {

    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB chunks

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
            uriBuilder = new URIBuilder(hostname);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        uriBuilder.addParameters(params);

        CloseableHttpResponse httpResponse;
        try {
            HttpPost request = new HttpPost(uriBuilder.build());
            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file);
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

    @Override
    public void uploadFile(File file, List<NameValuePair> params, boolean flag) {
        long fileSize = file.length();
        int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
        System.out.println("THE FILE SIZE IS: ");
        System.out.println(fileSize);
        System.out.println("THE TOTAL CHUNKS ARE: ");
        System.out.println(totalChunks);

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            for (int chunkNumber = 1; chunkNumber <= totalChunks; chunkNumber++) {
                int start = chunkNumber * CHUNK_SIZE;
                int chunkSize = Math.min(CHUNK_SIZE, (int) (fileSize - start));
                byte[] buffer = new byte[chunkSize];

                int bytesRead = fileInputStream.read(buffer, 0, chunkSize);
                if (bytesRead == -1) break;

                System.out.println("SENDING CHUNK # " + chunkNumber);
                sendChunk(hostname, buffer, chunkNumber, totalChunks, file.getName(), params);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendChunk(String serverUrl, byte[] chunk, int chunkNumber, int totalChunks, String fileName, List<NameValuePair> params) throws IOException {

        if (chunkNumber == 1) {
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

            System.out.println("Removing resumableChunkNumber... ");
            params.remove(params.get(1));
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

            System.out.println("Adding resumableChunkNumber... ");
            params.add(new BasicNameValuePair("resumableChunkNumber", Integer.toString(chunkNumber)));
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

            System.out.println("Removing resumableTotalChunks... ");
            params.remove(params.get(3));
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

            System.out.println("Adding resumableTotalChunks... ");
            params.add(new BasicNameValuePair("resumableTotalChunks", Integer.toString(totalChunks)));
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

//        System.out.println("Removing resumableFilename... ");
//        params.remove(new BasicNameValuePair("resumableFilename", ""));
//        System.out.println("THE PARAMS ARE: ");
//        System.out.println(params);
//
//        System.out.println("Adding resumableFilename... ");
//        params.add(new BasicNameValuePair("resumableFilename", fileName));
//        System.out.println("THE PARAMS ARE: ");
//        System.out.println(params);

        } else {
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

            System.out.println("Removing resumableChunkNumber... ");
            params.remove(params.get(10));
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

            System.out.println("Adding resumableChunkNumber... ");
            params.add(new BasicNameValuePair("resumableChunkNumber", Integer.toString(chunkNumber)));
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

            System.out.println("Removing resumableTotalChunks... ");
            params.remove(params.get(11));
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);

            System.out.println("Adding resumableTotalChunks... ");
            params.add(new BasicNameValuePair("resumableTotalChunks", Integer.toString(totalChunks)));
            System.out.println("THE PARAMS ARE: ");
            System.out.println(params);
        }

        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(hostname);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        uriBuilder.addParameters(params);
        System.out.println("THE URI BUILDER IS: ");
        System.out.println(uriBuilder);

        CloseableHttpResponse httpResponse;
        try {
            HttpPost request = new HttpPost(uriBuilder.build());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", chunk, ContentType.APPLICATION_OCTET_STREAM, fileName);
            builder.addTextBody("chunkNumber", String.valueOf(chunkNumber+1), ContentType.TEXT_PLAIN);
            builder.addTextBody("totalChunks", String.valueOf(totalChunks), ContentType.TEXT_PLAIN);
            builder.addTextBody("fileName", fileName, ContentType.TEXT_PLAIN);

            request.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int responseCode = response.getCode();
                if (responseCode == 200) {
                    System.out.println("Chunk " + chunkNumber + " uploaded successfully.");
                } else {
                    System.out.println("Failed to upload chunk " + chunkNumber + ": " + responseCode);
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