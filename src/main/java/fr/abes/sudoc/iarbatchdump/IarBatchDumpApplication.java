package fr.abes.sudoc.iarbatchdump;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IarBatchDumpApplication implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("exportNoticesJob_Romain")
    private Job exportNoticesJob; // rameauUploadJob, rameauVectorizationJob

    public static void main(String[] args) {
        SpringApplication.run(IarBatchDumpApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // JobParameters jobParameters = new JobParametersBuilder()
        //         .addString("exportAction", "update")
        //         .addLong("nbJours", 30L)
        //         .addString("outputFilePath", "/tmp")
        //         .toJobParameters();

        // jobLauncher.run(exportNoticesJob, jobParameters);
        


        //génère le csv à partir de la requête
        JobParameters jobParameters_requete = new JobParametersBuilder()
                .addString("exportAction", "update")
                .addLong("nbJours", 30L)
                .addString("outputFilePath", "/tmp")
                .addString("executionRequete", "true")
                .toJobParameters();

        jobLauncher.run(exportNoticesJob, jobParameters_requete);



        //génère le csv à partir de la procédure
        JobParameters jobParameters_procedure = new JobParametersBuilder()
                .addString("exportAction", "update")
                .addLong("nbJours", 30L)
                .addString("outputFilePath", "/tmp")
                .addString("executionRequete", "false")
                .toJobParameters();

        jobLauncher.run(exportNoticesJob, jobParameters_procedure);
    }
}