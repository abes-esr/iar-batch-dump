package fr.abes.sudoc.iarbatchdump.config;




import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;

@Configuration
public class ComparisonBatchTestConfig {



    @Bean("oracleJdbcTemplate")
    @Primary 
    @Profile("test")
    public JdbcTemplate oracleJdbcTemplateTest(@Qualifier("testDataSource") DataSource testDataSource) {
        return new JdbcTemplate(testDataSource);
    }    
    


    @Bean
    public Job noticeExportJobProcedureTest(
            JobRepository jobRepository,
            @Qualifier("exportNoticeProcedureStep") Step exportNoticeProcedureStep) {

        return new JobBuilder("noticeExportJobProcedureTest", jobRepository)
                .start(exportNoticeProcedureStep)
                .build();
    }

    @Bean
    public Step exportNoticeProcedureStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("noticeReaderProcedure") JdbcCursorItemReader<CsvRecord> noticeReader,
            FlatFileItemWriter<CsvRecord> csvWriter) {

        return new StepBuilder("exportNoticeProcedureStep", jobRepository)
                .<CsvRecord, CsvRecord>chunk( 1000, transactionManager )
                .reader(noticeReader)
                // pas besoin de processor ici, puisqu'il n'y a pas de traitement entre l'entrée et la sortie
                .writer(csvWriter)
                .build();
    }


}