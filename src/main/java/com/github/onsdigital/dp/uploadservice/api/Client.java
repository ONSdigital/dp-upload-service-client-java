package com.github.onsdigital.dp.uploadservice.api;

public interface Client {
    /**
     * Uploads the given file to the private s3 bucket.
     *
     * @param fileName
     */
    void uploadFile(String fileName);
}
