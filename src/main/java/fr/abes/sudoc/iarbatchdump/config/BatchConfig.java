package fr.abes.sudoc.iarbatchdump.config;

import fr.abes.sudoc.iarbatchdump.mapper.NoticeRowMapper;
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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


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




    // j'ai rajouté un paramètre pour qu'on exécute le même writer mais soit sur la requete, soit sur la procédure
    // (même traitement mais noms de fichiers différents)

    // J'ai aussi changé le type de "ItemWriter" à "FlatItemWriter" parce que... je sais plus mais ça marchait pas sinon 
    // (je crois un problème avec le step, qui avait un reader en JdbcCursorItemReader et un writer en truc pas compatible)
    @Bean
    @StepScope
    public FlatFileItemWriter<CsvRecord> csvWriter(
            @Value("#{jobParameters['outputFilePath']}") String outputFilePath,
            @Value("#{jobParameters['exportAction']}") String exportAction,
            @Value("#{jobParameters['executionRequete']}") String executionRequete

            ) {
        
        String filename;

        if(executionRequete.equals("true")){
            filename = "requete_sur_ppn_test.csv";
        }
        else{
            filename = "procedure_sur_ppn_test.csv";
        }

        FlatFileItemWriter<CsvRecord> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputFilePath + "/" + filename));
        writer.setAppendAllowed(false);
        writer.setLineAggregator(item -> item.toCsvLine());
        writer.setHeaderCallback(w -> w.write("ppn\tthese\ttitre\tresume\trameau\tlangue"));
        return writer;
    }



// exemple pour récupérer les résultats de la requête (job + step + reader)

    // job
    @Bean
    public Job exportNoticesJob_Romain(
            JobRepository jobRepository,
            Step export_notice_to_csv_Step) {

        return new JobBuilder("exportNoticesJob_Romain", jobRepository)
                .start(export_notice_to_csv_Step)
                .build();
    }


    // step
    @Bean
    public Step export_notice_to_csv_Step(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<CsvRecord> noticeReader,
            FlatFileItemWriter<CsvRecord> csvWriter) {

        return new StepBuilder("export_notice_to_csv_Step", jobRepository)
                .<CsvRecord, CsvRecord>chunk(
                        100,
                        transactionManager
                )
                .reader(noticeReader)
                // pas besoin de processor ici, puisqu'il n'y a pas de traitement entre l'entrée et la sortie
                .writer(csvWriter)
                .build();
    }


    // reader
    @Bean
    @StepScope
    public JdbcCursorItemReader<CsvRecord> noticeReader(
            DataSource dataSource,
            @Value("classpath:/sql/notices_test_query.sql") Resource sqlFile,
            @Value("#{jobParameters['executionRequete']}") String executionRequete
    ) throws IOException {
        

        String sql;

        // si on veut récupérer les résultats de la requête sql, il faut lire le fichier pour obtenir la requête du fichier.
        if(executionRequete.equals("true")){
            sql = new String(
                    sqlFile.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
        // si on veut récupérer les résultats de la procédure, on lit juste cette table 
        // (j'ai rempli manuellement cette table avec les résultats de la procédure)
        else{
            sql = "SELECT * FROM IAR_RESULTAT_ORIGINAL";
        }

    
        return new JdbcCursorItemReaderBuilder<CsvRecord>()
                .name("noticeReader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(new NoticeRowMapper())
                .build();
    }
}


