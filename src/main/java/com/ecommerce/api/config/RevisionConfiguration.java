package com.ecommerce.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@ConfigurationProperties(prefix = "revision")
@Getter
@Setter
public class RevisionConfiguration {

    /**
     * Retention period for revisions (in days)
     */
    private int retentionPeriod = 180; // 6 months by default

    /**
     * Maximum number of revisions to keep per entity
     */
    private int maxRevisionsPerEntity = 100;

    /**
     * Whether to track changes in collections
     */
    private boolean trackCollectionChanges = true;

    /**
     * Whether to track changes in transient fields
     */
    private boolean trackTransientFields = false;

    /**
     * Whether to track changes in lazy-loaded fields
     */
    private boolean trackLazyFields = false;

    /**
     * Fields to exclude from revision tracking
     */
    private String[] excludedFields = {};

    /**
     * Whether to store full entity state in revisions
     */
    private boolean storeFullState = false;

    /**
     * Whether to compress revision data
     */
    private boolean compressData = true;

    /**
     * Cleanup schedule (cron expression)
     */
    private String cleanupSchedule = "0 0 0 * * ?"; // Daily at midnight

    /**
     * Async executor configuration
     */
    private AsyncConfig asyncConfig = new AsyncConfig();

    @Getter
    @Setter
    public static class AsyncConfig {
        private int corePoolSize = 2;
        private int maxPoolSize = 5;
        private int queueCapacity = 500;
        private String threadNamePrefix = "RevisionAsync-";
        private Duration timeout = Duration.ofSeconds(60);
    }

    /**
     * Configure async executor
     */
    @Bean(name = "revisionAsyncExecutor")
    public Executor revisionAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncConfig.getCorePoolSize());
        executor.setMaxPoolSize(asyncConfig.getMaxPoolSize());
        executor.setQueueCapacity(asyncConfig.getQueueCapacity());
        executor.setThreadNamePrefix(asyncConfig.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds((int) asyncConfig.getTimeout().getSeconds());
        executor.initialize();
        return executor;
    }

    /**
     * Get retention period in milliseconds
     */
    public long getRetentionPeriodMillis() {
        return Duration.ofDays(retentionPeriod).toMillis();
    }

    /**
     * Check if field should be tracked
     */
    public boolean shouldTrackField(String fieldName, boolean isCollection, boolean isTransient, boolean isLazy) {
        // Check if field is explicitly excluded
        for (String excludedField : excludedFields) {
            if (fieldName.equals(excludedField)) {
                return false;
            }
        }

        // Check field characteristics
        if (isCollection && !trackCollectionChanges) {
            return false;
        }
        if (isTransient && !trackTransientFields) {
            return false;
        }
        if (isLazy && !trackLazyFields) {
            return false;
        }

        return true;
    }

    /**
     * Get cleanup threshold timestamp
     */
    public long getCleanupThresholdTimestamp() {
        return System.currentTimeMillis() - getRetentionPeriodMillis();
    }

    /**
     * Check if revision should be cleaned up
     */
    public boolean shouldCleanupRevision(long revisionTimestamp) {
        return revisionTimestamp < getCleanupThresholdTimestamp();
    }

    /**
     * Check if entity has too many revisions
     */
    public boolean hasTooManyRevisions(long revisionCount) {
        return revisionCount > maxRevisionsPerEntity;
    }

    /**
     * Get compression threshold
     */
    public int getCompressionThreshold() {
        return compressData ? 1024 : Integer.MAX_VALUE; // Compress data larger than 1KB
    }

    /**
     * Create revision data processor
     */
    @Bean
    public RevisionDataProcessor revisionDataProcessor() {
        return new RevisionDataProcessor(this);
    }

    /**
     * Revision data processor class
     */
    public static class RevisionDataProcessor {
        private final RevisionConfiguration config;

        public RevisionDataProcessor(RevisionConfiguration config) {
            this.config = config;
        }

        /**
         * Process revision data
         */
        public String processData(String data) {
            if (data == null || data.isEmpty()) {
                return data;
            }

            // Compress data if needed
            if (config.isCompressData() && data.length() > config.getCompressionThreshold()) {
                return compressData(data);
            }

            return data;
        }

        /**
         * Compress data
         */
        private String compressData(String data) {
            try {
                java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                java.util.zip.GZIPOutputStream gzipOutputStream = 
                    new java.util.zip.GZIPOutputStream(outputStream);
                gzipOutputStream.write(data.getBytes());
                gzipOutputStream.close();
                return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
            } catch (Exception e) {
                return data;
            }
        }

        /**
         * Decompress data
         */
        public String decompressData(String compressedData) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(compressedData);
                java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(decoded);
                java.util.zip.GZIPInputStream gzipInputStream = 
                    new java.util.zip.GZIPInputStream(inputStream);
                java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                gzipInputStream.close();
                outputStream.close();
                return outputStream.toString();
            } catch (Exception e) {
                return compressedData;
            }
        }
    }
}
