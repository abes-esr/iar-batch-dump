package com.levant.iar.batch.job;

import com.levant.iar.batch.model.PpnData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Job configuration for extracting PPN data with Rameau subjects from Oracle database.
 * Uses JdbcCursorItemReader to read data using the SQL query from file.
 */
@Configuration
public class RameauExtractJob {

    private static final String JOB_NAME = "rameauExtractJob";
    private static final String STEP_NAME = "extractPpnStep";

    /**
     * Processor that logs the PPN data being processed.
     * Can be extended to add enrichment logic.
     */
    @Bean
    public ItemProcessor<PpnData, PpnData> ppnItemProcessor() {
        return ppnData -> {
            // Log processing for debugging
            // Can add enrichment logic here (e.g., transform data, add metadata)
            return ppnData;
        };
    }

    /**
     * Writer that logs the processed PPN data.
     * Can be replaced with actual output (CSV, database, etc.)
     */
    @Bean
    public ItemWriter<PpnData> ppnItemWriter() {
        return items -> {
            for (PpnData ppnData : items) {
                // Log or write to output
                // In production, this would write to CSV, database, or other destination
            }
        };
    }

    /**
     * Creates the extraction step for PPN data.
     * 
     * @param jobRepository the job repository
     * @param transactionManager the transaction manager
     * @param ppnItemReader the PPN item reader
     * @return configured Step
     */
    @Bean
    public Step extractPpnStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("ppnItemReader") JdbcCursorItemReader<PpnData> ppnItemReader) {
        
        return new StepBuilder(STEP_NAME, jobRepository)
                .<PpnData, PpnData>chunk(100, transactionManager)
                .reader(ppnItemReader)
                .processor(ppnItemProcessor())
                .writer(ppnItemWriter())
                .build();
    }

    /**
     * Creates the Rameau extraction job.
     * This job extracts PPN data with Rameau subjects from Oracle database.
     * 
     * @param jobRepository the job repository
     * @param extractPpnStep the extraction step
     * @return configured Job
     */
    @Bean
    public Job rameauExtractJob(
            JobRepository jobRepository,
            @Qualifier("extractPpnStep") Step extractPpnStep) {
        
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(extractPpnStep)
                .build();
    }
}
