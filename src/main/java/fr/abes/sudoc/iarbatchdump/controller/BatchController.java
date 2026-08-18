package fr.abes.sudoc.iarbatchdump.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch/rameau")
public class BatchController {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job rameauExtractJob;
    @Autowired
    private Job rameauUploadJob;
    @Autowired
    private Job rameauVectorizationJob;

    // Lancer l'extraction + génération CSV
    @PostMapping("/extract")
    public String launchExtractJob(
            @RequestParam String exportAction,
            @RequestParam(defaultValue = "1") long nbJours,
            @RequestParam(defaultValue = "/globule_pastel_rocou") String outputFilePath) throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("exportAction", exportAction)
                .addLong("nbJours", nbJours)
                .addString("outputFilePath", outputFilePath)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(rameauExtractJob, params);
        return "Job d'extraction lancé avec succès (action: " + exportAction + ")";
    }

    // Lancer l'upload du fichier
    @PostMapping("/upload")
    public String launchUploadJob(
            @RequestParam String exportAction,
            @RequestParam String outputFilePath) throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("exportAction", exportAction)
                .addString("outputFilePath", outputFilePath)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(rameauUploadJob, params);
        return "Job d'upload lancé avec succès (fichier: " + exportAction + ")";
    }

    // Lancer la vectorisation
    @PostMapping("/vectorize")
    public String launchVectorizationJob(
            @RequestParam(defaultValue = "update") String action,
            @RequestParam(defaultValue = "concepts") String conceptsORchains,
            @RequestParam(defaultValue = "allMin") String aliasModel,
            @RequestParam(defaultValue = "only_mono") String avecThese) throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("action", action)
                .addString("conceptsORchains", conceptsORchains)
                .addString("aliasModel", aliasModel)
                .addString("avecThese", avecThese)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(rameauVectorizationJob, params);
        return "Job de vectorisation lancé avec succès";
    }
}