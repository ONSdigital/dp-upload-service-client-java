package com.github.onsdigital.dp.uploadservice.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.NameValuePair;

public interface Client {
    /**
     * Uploads the given file to the private s3 bucket.
     *
     * @param file
     * @param params
     */
    void uploadResumableFile(File file, List<NameValuePair> params) throws IOException;
}
