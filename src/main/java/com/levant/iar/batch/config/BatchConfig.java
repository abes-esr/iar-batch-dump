package com.levant.iar.batch.config;

import com.levant.iar.batch.model.PpnData;
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
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    // ======================
    // 1. TRANSACTION MANAGER
    // ======================
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    // ======================
    // 2. JDBC TEMPLATE ORACLE
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
            ItemReader<PpnData> ppnReader,
            ItemProcessor<PpnData, PpnData> ppnProcessor,
            ItemWriter<PpnData> csvWriter) {
        return new StepBuilder("extractStep", jobRepository)
                .<PpnData, PpnData>chunk(1000, transactionManager)
                .reader(ppnReader)
                .processor(ppnProcessor)
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
            @Value("${app.batch.output-dir:/tmp}") String outputDir) {
        return new StepBuilder("uploadStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String exportAction = chunkContext.getStepContext().getStepExecution()
                            .getJobParameters().getString("exportAction");
                    String filename = "update".equals(exportAction) ?
                            "export_rameau_update.csv" : "export_rameau.csv";
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
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("vectorizationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParams = chunkContext.getStepContext().getStepExecution().getJobParameters();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // ======================
    // 5. READER, PROCESSOR, WRITER
    // ======================

    @Bean
    @StepScope
    public ItemReader<PpnData> ppnReader(
            @Qualifier("oracleDataSource") DataSource oracleDataSource) throws IOException {
        
        String sqlQuery = loadSqlQuery("sql/queries/select_ppn_with_rameau.sql");
        
        return new JdbcCursorItemReaderBuilder<PpnData>()
                .name("ppnItemReader")
                .dataSource(oracleDataSource)
                .sql(sqlQuery)
                .rowMapper(new PpnDataRowMapper())
                .verifyCursorPosition(true)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<PpnData, PpnData> ppnProcessor() {
        return ppnData -> ppnData;
    }

    @Bean
    @StepScope
    public ItemWriter<PpnData> csvWriter(
            @Value("#{jobParameters['outputFilePath']}") String outputFilePath,
            @Value("#{jobParameters['exportAction']}") String exportAction) {

        String filename = "update".equals(exportAction) ?
                "export_rameau_update.csv" : "export_rameau.csv";

        FlatFileItemWriter<PpnData> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputFilePath + "/" + filename));
        writer.setAppendAllowed(false);
        writer.setLineAggregator(item -> formatCsvLine(item));
        writer.setHeaderCallback(w -> w.write("ppn\tppn_these\ttitre\tresume\tlangue\trameau"));
        return writer;
    }

    // ======================
    // HELPER METHODS
    // ======================

    private String loadSqlQuery(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static class PpnDataRowMapper implements RowMapper<PpnData> {
        @Override
        public PpnData mapRow(ResultSet rs, int rowNum) throws SQLException {
            PpnData ppnData = new PpnData();
            ppnData.setPpn(rs.getString("ppn"));
            ppnData.setPpnThese(rs.getString("ppn_these"));
            ppnData.setTitre(rs.getString("titre"));
            ppnData.setResume(rs.getString("resume"));
            ppnData.setLangue(rs.getString("langue"));
            ppnData.setRameau(rs.getString("rameau"));
            return ppnData;
        }
    }

    private String formatCsvLine(PpnData ppnData) {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s",
                escapeCsv(ppnData.getPpn()),
                escapeCsv(ppnData.getPpnThese()),
                escapeCsv(ppnData.getTitre()),
                escapeCsv(ppnData.getResume()),
                escapeCsv(ppnData.getLangue()),
                escapeCsv(ppnData.getRameau()));
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\t", " ")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
