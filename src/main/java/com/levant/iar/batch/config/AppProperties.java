package com.levant.iar.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the IAR Batch application.
 * These properties can be configured in application.yml or via environment variables.
 */
@Configuration
@ConfigurationProperties(prefix = "app.batch")
public class AppProperties {

    /**
     * Size of chunks for batch processing.
     */
    private int chunkSize = 1000;

    /**
     * Maximum number of parallel readers.
     */
    private int maxReaders = 5;

    /**
     * Temporary directory for batch processing.
     */
    private String tempDirectory = "/tmp/iar-batch";

    /**
     * Timeout for Oracle database operations (in milliseconds).
     */
    private long oracleTimeout = 30000;

    /**
     * Whether to enable retry on failure.
     */
    private boolean retryEnabled = true;

    /**
     * Maximum number of retry attempts.
     */
    private int maxRetries = 3;

    /**
     * Path to the SQL query file for PPN extraction.
     */
    private String ppnQueryFile = "sql/queries/select_ppn_with_rameau.sql";

    // Getters and Setters

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getMaxReaders() {
        return maxReaders;
    }

    public void setMaxReaders(int maxReaders) {
        this.maxReaders = maxReaders;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public long getOracleTimeout() {
        return oracleTimeout;
    }

    public void setOracleTimeout(long oracleTimeout) {
        this.oracleTimeout = oracleTimeout;
    }

    public boolean isRetryEnabled() {
        return retryEnabled;
    }

    public void setRetryEnabled(boolean retryEnabled) {
        this.retryEnabled = retryEnabled;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getPpnQueryFile() {
        return ppnQueryFile;
    }

    public void setPpnQueryFile(String ppnQueryFile) {
        this.ppnQueryFile = ppnQueryFile;
    }
}
