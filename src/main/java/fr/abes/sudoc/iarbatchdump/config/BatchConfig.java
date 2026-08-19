package fr.abes.sudoc.iarbatchdump.config;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;
import fr.abes.sudoc.iarbatchdump.model.RameauExportParams;
import fr.abes.sudoc.iarbatchdump.processor.RameauDataProcessor;
import fr.abes.sudoc.iarbatchdump.reader.SqlFilePpnReader;
import fr.abes.sudoc.iarbatchdump.service.FileUploadService;
import fr.abes.sudoc.iarbatchdump.service.VectorizationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    @Bean
    public JdbcTemplate oracleJdbcTemplate(@Qualifier("oracleDataSource") DataSource oracleDataSource) {
        return new JdbcTemplate(oracleDataSource);
    }

    // ======================
    // 3. REST TEMPLATE
    // ======================
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ======================
    // 4. JOBS
    // ======================

    // ---------- JOB 1 : EXTRACTION + CSV ----------
    @Bean
    public Job rameauExtractJob(JobRepository jobRepository, Step extractStep) {
        return new JobBuilder("rameauExtractJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(extractStep)
                .build();
    }

    @Bean
    public Step extractStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<String> ppnReader,
            ItemProcessor<String, CsvRecord> rameauProcessor,
            ItemWriter<CsvRecord> csvWriter) {
        return new StepBuilder("extractStep", jobRepository)
                .<String, CsvRecord>chunk(1000, transactionManager)
                .reader(ppnReader)
                .processor(rameauProcessor)
                .writer(csvWriter)
                .faultTolerant()
                .skipLimit(100)
                .skip(Exception.class)
                .build();
    }

    // ---------- JOB 2 : UPLOAD ----------
    @Bean
    public Job rameauUploadJob(JobRepository jobRepository, Step uploadStep) {
        return new JobBuilder("rameauUploadJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(uploadStep)
                .build();
    }

    @Bean
    public Step uploadStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FileUploadService fileUploadService,
            @Value("${app.batch.output-dir:/tmp}") String outputDir) {
        return new StepBuilder("uploadStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String exportAction = chunkContext.getStepContext().getStepExecution()
                            .getJobParameters().getString("exportAction");
                    String filename = "update".equals(exportAction) ?
                            "export_rameau_update.csv" : "export_rameau.csv";
                    fileUploadService.uploadFile(outputDir + "/" + filename);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // ---------- JOB 3 : VECTORIZATION ----------
    @Bean
    public Job rameauVectorizationJob(JobRepository jobRepository, Step vectorizationStep) {
        return new JobBuilder("rameauVectorizationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(vectorizationStep)
                .build();
    }

    @Bean
    public Step vectorizationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            VectorizationService vectorizationService) {
        return new StepBuilder("vectorizationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParams = chunkContext.getStepContext().getStepExecution().getJobParameters();
                    RameauExportParams params = RameauExportParams.builder()
                            .action(jobParams.getString("action"))
                            .conceptsORchains(jobParams.getString("conceptsORchains"))
                            .aliasModel(jobParams.getString("aliasModel"))
                            .avecThese(jobParams.getString("avecThese"))
                            .build();
                    vectorizationService.launchVectorization(params);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // ======================
    // 5. READER, PROCESSOR, WRITER
    // ======================

    @Bean
    @StepScope
    public ItemReader<String> ppnReader(
            @Qualifier("oracleDataSource") DataSource oracleDataSource,
            @Value("#{jobParameters['exportAction']}") String exportAction,
            @Value("#{jobParameters['nbJours']}") int nbJours) {

        SqlFilePpnReader reader = new SqlFilePpnReader(
                new JdbcTemplate(oracleDataSource),
                new ClassPathResource("sql/ppn_query.sql")
        );
        
        // Configurer les paramètres
        RameauExportParams params = RameauExportParams.builder()
                .exportAction(exportAction)
                .nbJours(nbJours)
                .build();
        reader.setParams(params);
        
        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<String, CsvRecord> rameauProcessor(JdbcTemplate oracleJdbcTemplate) {
        return new RameauDataProcessor(oracleJdbcTemplate);
    }

    @Bean
    @StepScope
    public ItemWriter<CsvRecord> csvWriter(
            @Value("#{jobParameters['outputFilePath']}") String outputFilePath,
            @Value("#{jobParameters['exportAction']}") String exportAction) {

        String filename = "update".equals(exportAction) ?
                "export_rameau_update.csv" : "export_rameau.csv";

        FlatFileItemWriter<CsvRecord> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputFilePath + "/" + filename));
        writer.setAppendAllowed(false);
        writer.setLineAggregator(item -> item.toCsvLine());
        writer.setHeaderCallback(w -> w.write("ppn\tthese\ttitre\tresume\trameau\tlangue"));
        return writer;
    }
}
