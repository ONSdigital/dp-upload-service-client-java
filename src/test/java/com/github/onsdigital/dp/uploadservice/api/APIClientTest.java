package com.github.onsdigital.dp.uploadservice.api;

import com.github.onsdigital.dp.uploadservice.api.exceptions.ConnectionException;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class APIClientTest {

    public static final String TOKEN = "AUTHENTICATION-TOKEN";

    private List<NameValuePair> params;
    private File file;

    @BeforeEach
    void setup() {
        params = new ArrayList<NameValuePair>(12);
        params.add(new BasicNameValuePair("resumableFilename", "file"));
        params.add(new BasicNameValuePair("resumableChunkNumber", "1"));
        params.add(new BasicNameValuePair("resumableType", "text/plain"));
        params.add(new BasicNameValuePair("resumableTotalChunks", "1"));
        params.add(new BasicNameValuePair("resumableChunkSize", "1000000"));
        params.add(new BasicNameValuePair("path", "testing"));
        params.add(new BasicNameValuePair("isPublishable", "false"));
        params.add(new BasicNameValuePair("resumableTotalSize", "500000"));
        params.add(new BasicNameValuePair("type", "text/plain"));
        params.add(new BasicNameValuePair("licence", "fran"));
        params.add(new BasicNameValuePair("licenceUrl", "google"));
        params.add(new BasicNameValuePair("collectionId","collectionID"));
        file = new File(getClass().getClassLoader().getResource("testFile.txt").getFile());
    }

    @Test
    void successfullyUploadingAFile() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_CREATED));

        HttpUrl url = server.url("");

        APIClient client = new APIClient(url.toString(), TOKEN);

        try {
            client.uploadFile(file, params);
        } catch (Exception e) {
            Assertions.fail("No Exception should have been thrown");
        }

    }

    @Test
    void handingInvalidHostnameProvided() {
        APIClient client = new APIClient("NOT A VALID HOSTNAME", TOKEN);

        Exception e = assertThrows(RuntimeException.class, () -> {
            client.uploadFile(file, params);
        });

        assertNotNull(e.getCause());
    }

    @Test
    void handingIncorrectHostnameProvided() {
        APIClient client = new APIClient("http://localhost:123456789", TOKEN);

        Exception e = assertThrows(ConnectionException.class, () -> {
            client.uploadFile(file, params);
        });

        assertNotNull(e.getCause());
    }
}