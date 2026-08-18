package fr.abes.sudoc.iarbatchdump;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IarBatchDumpApplication implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job rameauExtractJob; // rameauUploadJob, rameauVectorizationJob

    public static void main(String[] args) {
        SpringApplication.run(IarBatchDumpApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("exportAction", "update")
                .addLong("nbJours", 30L)
                .addString("outputFilePath", "/tmp")
                .toJobParameters();

        jobLauncher.run(rameauExtractJob, jobParameters);
    }
}