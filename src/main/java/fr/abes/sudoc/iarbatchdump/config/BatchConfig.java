package fr.abes.sudoc.iarbatchdump.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;
import fr.abes.sudoc.iarbatchdump.processor.DeduplicateNoticesProcessor;


@Configuration
@EnableBatchProcessing
public class BatchConfig {

    // ======================
    // 1. TRANSACTION MANAGER (H2 pour les métadonnées Spring Batch)
    // ======================
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    // ======================
    // 2. JDBC TEMPLATE ORACLE (pour les requêtes métiers)
    // ======================

    @Bean("oracleJdbcTemplate")
    @Profile("!test")
    @Primary 
    public JdbcTemplate oracleJdbcTemplate(@Qualifier("oracleDataSource") DataSource oracleDataSource) {
        return new JdbcTemplate(oracleDataSource);
    }

    

    // ======================
    // JOBS
    // ======================


    @Bean
    public Job noticeExportJob(
            JobRepository jobRepository,
            @Qualifier("exportNoticesStep") Step exportNoticesStep,
            @Qualifier("deduplicateNoticesStep") Step deduplicateCsvStep
        ) {

        return new JobBuilder("noticeExportJob", jobRepository)
                .start(exportNoticesStep)
                .next(deduplicateCsvStep)
                .build();
    }



    // ======================
    // STEPS
    // ======================


    // step worker (celui qui read et write)
    @Bean
    public Step exportNoticeWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<CsvRecord> noticeReader,
            FlatFileItemWriter<CsvRecord> csvWriter) {

        return new StepBuilder("exportNoticeWorkerStep", jobRepository)
                .<CsvRecord, CsvRecord>chunk(
                        1000,
                        transactionManager
                )
                .reader(noticeReader)
                // pas besoin de processor ici, puisqu'il n'y a pas de traitement entre l'entrée et la sortie
                .writer(csvWriter)
                .build();
    }



    // step "chef d'orchestre", qui lance le step de lecture et d'écriture plusieurs fois mais sur différentes partitions
    @Bean
    public Step exportNoticesStep(
            JobRepository jobRepository,
            @Qualifier("exportNoticeWorkerStep") Step exportNoticeWorkerStep,
            Partitioner noticePartitioner) {

        return new StepBuilder("exportNoticesStep", jobRepository)
                .partitioner(
                        "exportNoticesWorkerStep",
                        noticePartitioner
                )
                .step(exportNoticeWorkerStep)
                .gridSize(1)
                .build();
    }

    

    // step qui supprime les doublons du fichier
    @Bean
    public Step deduplicateNoticesStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        FlatFileItemReader<CsvRecord> deduplicationReader,
        DeduplicateNoticesProcessor processor,
        @Qualifier("deduplicationCsvWriter") FlatFileItemWriter<CsvRecord> deduplicatedCsvWriter) {

        return new StepBuilder(
                "deduplicateNoticesStep",
                jobRepository
        )
        .<CsvRecord, CsvRecord>chunk(
                1000,
                transactionManager
        )
        .reader(deduplicationReader)
        .processor(processor)
        .writer(deduplicatedCsvWriter)
        .build();
    }

}