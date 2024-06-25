package com.github.onsdigital.dp.uploadservice.api.configuration;

import org.apache.commons.lang3.StringUtils;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

public class Configuration {

    public static final String CHUNK_SIZE = "CHUNK_SIZE";

    private int chunkSize = 1024 * 1024 * 5; // defaults to 5MB chunks

    public Configuration() {
        String chunkSizeStr = getValue(CHUNK_SIZE);

        // Set up the configuration:
        configureChunkSize(chunkSizeStr);
    }

    void configureChunkSize(String chunkSizeStr) {
        if (StringUtils.isNotBlank(chunkSizeStr)) {
            try {
                this.chunkSize = Integer.parseInt(chunkSizeStr);
                info().log("Using chunk size " + this.chunkSize );
            } catch (NumberFormatException e) {
                info().log("Unable to parse CHUNK_SIZE variable (" + chunkSizeStr + "). Defaulting to chunk size " + this.chunkSize );
            }
        } else {
            info().log("Environmental variable CHUNK_SIZE is not set. Defaulting to chunk size " + this.chunkSize );
        }
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        // Parameters:
        result.append("\nEnvironment/property values:");
        result.append("\n * " + CHUNK_SIZE + "=" + getValue(CHUNK_SIZE));

        // Resolved configuration:
        result.append("\nResolved configuration:");
        result.append("\n - chunkSize:\t" + chunkSize);

        return result.toString();
    }

    static String getValue(String key) {
        String result = StringUtils.defaultIfBlank(System.getProperty(key), StringUtils.EMPTY);
        result = StringUtils.defaultIfBlank(result, System.getenv(key));
        return result;
    }

    public int getChunkSize() {
        return chunkSize;
    }
}
