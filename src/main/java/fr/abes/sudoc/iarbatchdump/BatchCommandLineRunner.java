package fr.abes.sudoc.iarbatchdump;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "app.batch.enabled",
    havingValue = "true"
)
public class BatchCommandLineRunner implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("noticeExportJob")
    private Job noticeExportJob;


    @Value("${app.batch.action}")
    private String action;

    @Value("${app.batch.output-directory}")
    private String outputFilePath;

    @Value("${app.batch.output-filename}")
    private String outputFilename;

    @Override
    public void run(String... args) throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("action", action)
                .addString("outputFilePath", outputFilePath)
                .addString("sqlFile", "notices_query.sql")
                .addString("filename", outputFilename)
                .toJobParameters();

        jobLauncher.run(noticeExportJob, jobParameters);
    }
}
