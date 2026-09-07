package fr.abes.sudoc.iarbatchdump.config;


import java.io.IOException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import fr.abes.sudoc.iarbatchdump.mapper.NoticeRowMapper;
import fr.abes.sudoc.iarbatchdump.model.CsvRecord;
import fr.abes.sudoc.iarbatchdump.processor.DeduplicateNoticesProcessor;

@TestConfiguration
public class BatchConfigTest {


    // generation csv avec la procedure
    @Bean
    public Job procedureNoticeTestExportJob(
            JobRepository jobRepository,
            @Qualifier("procedureExportNoticeWorkerStep") Step exportNoticesStep
        ) {

        return new JobBuilder("procedureNoticeTestExportJob", jobRepository)
                .start(exportNoticesStep)
                .build();
    }


    // step worker (celui qui read et write) procedure
    @Bean
    public Step procedureExportNoticeWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("noticeReaderProcedureTest") JdbcCursorItemReader<CsvRecord> noticeReader,
            FlatFileItemWriter<CsvRecord> csvWriter) {

        return new StepBuilder("procedureExportNoticeWorkerStep", jobRepository)
                .<CsvRecord, CsvRecord>chunk(
                        1000,
                        transactionManager
                )
                .reader(noticeReader)
                // pas besoin de processor ici, puisqu'il n'y a pas de traitement entre l'entrée et la sortie
                .writer(csvWriter)
                .build();
    }


    // reader qui lit le résultat de la procédure (la procédure a été lancée à la main)
    @Bean
    @StepScope
    public JdbcCursorItemReader<CsvRecord> noticeReaderProcedureTest(
            DataSource dataSource
    ) throws IOException {
        

        String sql = "SELECT * FROM IAR_RESULTAT_ORIGINAL";
        
    
        return new JdbcCursorItemReaderBuilder<CsvRecord>()
                .name("noticeReaderProcedureTest")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(new NoticeRowMapper())
                .build();
    }


    
    


    // generation csv avec la requete
    @Bean
    public Job requeteNoticeTestExportJob(
            JobRepository jobRepository,
            @Qualifier("requeteExportNoticeWorkerStep") Step exportNoticesStep
        ) {

        return new JobBuilder("requeteNoticeTestExportJob", jobRepository)
                .start(exportNoticesStep)
                .build();
    }


    // step worker (celui qui read et write) procedure
    @Bean
    public Step requeteExportNoticeWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("noticeReaderRequeteTest") JdbcCursorItemReader<CsvRecord> noticeReader,
            FlatFileItemWriter<CsvRecord> csvWriter) {

        return new StepBuilder("requeteExportNoticeWorkerStep", jobRepository)
                .<CsvRecord, CsvRecord>chunk(
                        1000,
                        transactionManager
                )
                .reader(noticeReader)
                // pas besoin de processor ici, puisqu'il n'y a pas de traitement entre l'entrée et la sortie
                .writer(csvWriter)
                .build();
    }


    // reader qui lit le résultat de la procédure (la procédure a été lancée à la main)
    @Bean
    @StepScope
    public JdbcCursorItemReader<CsvRecord> noticeReaderRequeteTest(
            DataSource dataSource
    ) throws IOException {
        

        String sql = "SELECT * FROM IAR_RESULTAT_NOUVEAU";
        
    
        return new JdbcCursorItemReaderBuilder<CsvRecord>()
                .name("noticeReaderRequeteTest")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(new NoticeRowMapper())
                .build();
    }











    // job temporaire de dédoublonnage 
    // @Bean
    // public Job deduplicateJob(
    //         JobRepository jobRepository,
    //         //@Qualifier("exportNoticesStep") Step exportNoticesStep,
    //         @Qualifier("deduplicateNoticesStep") Step deduplicateCsvStep) {

    //     return new JobBuilder("deduplicateJob", jobRepository)
    //             .start(deduplicateCsvStep)
    //             .build();
    // }


    


    



    // // step qui supprime les doublons du fichier
    // @Bean
    // public Step deduplicateNoticesStep(
    //     JobRepository jobRepository,
    //     PlatformTransactionManager transactionManager,
    //     FlatFileItemReader<CsvRecord> deduplicationReader,
    //     DeduplicateNoticesProcessor processor,
    //     @Qualifier("deduplicationCsvWriter") FlatFileItemWriter<CsvRecord> deduplicatedCsvWriter) {

    //     return new StepBuilder(
    //             "deduplicateNoticesStep",
    //             jobRepository
    //     )
    //     .<CsvRecord, CsvRecord>chunk(
    //             1000,
    //             transactionManager
    //     )
    //     .reader(deduplicationReader)
    //     .processor(processor)
    //     .writer(deduplicatedCsvWriter)
    //     .build();
    // }

}
