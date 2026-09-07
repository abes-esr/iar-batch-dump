package fr.abes.sudoc.iarbatchdump;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import fr.abes.sudoc.iarbatchdump.config.BatchConfigTest;


@SpringBootTest
@Import(BatchConfigTest.class)
public class batchDumpTest {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("requeteNoticeTestExportJob")
    private Job requeteNoticeExportJob; 


    @Autowired
    @Qualifier("procedureNoticeTestExportJob")
    private Job procedureNoticeExportJob; 


     @Test
    void testNoticeExportJob() throws Exception {

        // génère le csv à partir de la requete
        // JobParameters jobParameters_procedure = new JobParametersBuilder()
        //         .addString("outputFilePath", "/tmp")
        //         .addString("filename", "requete_sur_ppns_test.csv")
        //         .toJobParameters();

        // jobLauncher.run(requeteNoticeExportJob, jobParameters_procedure);



        // génère le csv à partir de la procédure
        JobParameters jobParameters_procedure = new JobParametersBuilder()
                .addString("outputFilePath", "/tmp")
                .addString("filename", "procedure_full.csv")
                .toJobParameters();

        jobLauncher.run(procedureNoticeExportJob, jobParameters_procedure);

    }
}
