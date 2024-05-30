package com.github.onsdigital.dp.uploadservice.api;

import org.apache.http.NameValuePair;

import java.io.File;
import java.util.List;

public interface Client {
    /**
     * Uploads the given file to the private s3 bucket.
     *
     * @param file
     * @param params
     */
    void uploadFile(File file, List<NameValuePair> params);
}
