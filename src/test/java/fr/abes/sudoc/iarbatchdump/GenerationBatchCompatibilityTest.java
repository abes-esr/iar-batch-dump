package fr.abes.sudoc.iarbatchdump;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;
import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class GenerationComparisonTest {

    

    @Autowired
    private JobLauncher jobLauncher;

    
    @Autowired
    @Qualifier("noticeExportJobProcedureTest")
    private Job jobProcedure;
    
    @Autowired
    @Qualifier("noticeExportJob")
    private Job jobRequete;


    @Value("${app.batch.output-directory}")
    private String outputFilePath;


    private final JdbcTemplate comparisonJdbcTemplate;

    @Autowired
    GenerationComparisonTest(
            @Qualifier("oracleJdbcTemplate")
            JdbcTemplate jdbcTemplate
    ) {
        this.comparisonJdbcTemplate = jdbcTemplate;
    }





    @Test
    void procedureEtSqlDoiventProduireLeMemeCsv() throws Exception {

        

        // 1. Exécution de la procédure historique (vide la table et la remplit)
        comparisonJdbcTemplate.execute("TRUNCATE TABLE IAR_RESULTAT_PROCEDURE");
        comparisonJdbcTemplate.execute( "BEGIN IAR_QE_EXPORT_RAMEAU_TEST(); END;" );



        // 2. Transformer le résultat de la procédure en CSV
        JobParameters jobParametersProcedure = new JobParametersBuilder()
                .addString("outputFilePath", outputFilePath)
                .addString("filename", "procedure_test.csv")
                .toJobParameters();

        jobLauncher.run(jobProcedure , jobParametersProcedure );


        // 3. faire la même chose mais avec la requête SQL
        JobParameters jobParametersQuery = new JobParametersBuilder()
                .addString("outputFilePath", outputFilePath)
                .addString("sqlFile", "notices_query_test.sql")
                .addString("filename", "requete_test.csv")
                .toJobParameters();
                
        jobLauncher.run(jobRequete , jobParametersQuery );

        // 4. Comparer les deux CSV
        comparerCsv();
    }

    

    
   
    private void comparerCsv() throws IOException {

    Path fichierProcedure = Path.of(outputFilePath + "/procedure_test.csv");
    Path fichierRequete = Path.of(outputFilePath + "/requete_test.csv");

    Map<String, CsvRecord> procedureRecords = lireCsv(fichierProcedure);
    Map<String, CsvRecord> requeteRecords = lireCsv(fichierRequete);

    // Vérification du nombre de notices
    assertEquals(
            procedureRecords.size(),
            requeteRecords.size(),
            "Les deux fichiers ne contiennent pas le même nombre de notices"
    );

    // PPN présents uniquement dans le fichier de la procédure
    Set<String> uniquementProcedure =
            new HashSet<>(procedureRecords.keySet());

    uniquementProcedure.removeAll(requeteRecords.keySet());

    assertTrue(
            uniquementProcedure.isEmpty(),
            "PPN présents uniquement dans procedure_test.csv : "
                    + uniquementProcedure
    );

    // PPN présents uniquement dans le fichier de la requête
    Set<String> uniquementRequete =
            new HashSet<>(requeteRecords.keySet());

    uniquementRequete.removeAll(procedureRecords.keySet());

    assertTrue(
            uniquementRequete.isEmpty(),
            "PPN présents uniquement dans requete_test.csv : "
                    + uniquementRequete
    );

    // Comparaison des notices
    List<String> differences = new ArrayList<>();

    for (String ppn : procedureRecords.keySet()) {

        CsvRecord procedure = procedureRecords.get(ppn);
        CsvRecord requete = requeteRecords.get(ppn);

        if (requete != null) {
            comparerNotice(procedure, requete, differences);
        }
    }

    assertTrue(
            differences.isEmpty(),
            () -> "Des différences ont été détectées :\n"
                    + String.join("\n", differences)
    );
}







    // pour charger un fichier csv
    private Map<String, CsvRecord> lireCsv(Path fichier) throws IOException {

    Map<String, CsvRecord> records = new HashMap<>();

    try (BufferedReader reader = Files.newBufferedReader(fichier)) {

        // Ignorer l'en-tête
        reader.readLine();

        String line;
        long lineNumber = 1;

        while ((line = reader.readLine()) != null) {

            lineNumber++;

            String[] fields = line.split("\t", -1);

            if (fields.length != 6) {
                throw new AssertionError( "Nombre de colonnes incorrect dans " + fichier + " à la ligne " + lineNumber + " : " + fields.length );
            }

            CsvRecord record = CsvRecord.builder()
                    .ppn(fields[0])
                    .these(fields[1])
                    .titre(fields[2])
                    .resume(fields[3])
                    .libelleRameau(fields[4])
                    .langue(fields[5])
                    .build();

            if (records.put(record.getPpn(), record) != null) {
                log.warn("fichier " + fichier.toString() + ", PPN en double : " + record.getPpn());
            }
        }
    }

    return records;
}



    private String normaliserRameau(String rameau) {

        if (rameau == null || rameau.isBlank()) {
            return "";
        }

        return Arrays.stream(rameau.split(";"))
                .map(String::trim)
                .filter(zone -> !zone.isEmpty())
                .map(this::normaliserZoneRameau)
                .sorted()
                .collect(Collectors.joining(";"));
    }


    private String normaliserZoneRameau(String zone) {

        return Arrays.stream(zone.split("--"))
                .map(String::trim)
                .filter(element -> !element.isEmpty())
                .sorted()
                .collect(Collectors.joining("--"));
    }



    private void comparerNotice( CsvRecord procedure, CsvRecord requete, List<String> differences) {

        String ppn = procedure.getPpn();

        comparerChamp( ppn, "PPN", procedure.getPpn(), requete.getPpn(), differences );

        comparerChamp( ppn, "THESE", procedure.getThese(), requete.getThese(), differences );

        comparerChamp( ppn, "TITRE", procedure.getTitre(), requete.getTitre(), differences );

        comparerChamp( ppn, "RESUME", procedure.getResume(), requete.getResume(), differences );

        comparerChamp( ppn, "LANGUE", procedure.getLangue(), requete.getLangue(), differences );

        comparerChamp(ppn, "RAMEAU",  normaliserRameau(procedure.getLibelleRameau()),  normaliserRameau(requete.getLibelleRameau()), differences);

    }

    private void comparerChamp(
        String ppn,
        String nomChamp,
        String valeurProcedure,
        String valeurRequete,
        List<String> differences) {

        if (!Objects.equals(valeurProcedure, valeurRequete)) {

            differences.add(
                    "PPN " + ppn
                            + " - " + nomChamp
                            + " différent : procédure=["
                            + valeurProcedure
                            + "], requête=["
                            + valeurRequete
                            + "]"
            );
        }
    }
}