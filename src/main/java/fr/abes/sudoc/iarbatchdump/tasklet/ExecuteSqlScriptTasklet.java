package fr.abes.sudoc.iarbatchdump.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ExecuteSqlScriptTasklet implements Tasklet {

    private final DataSource dataSource;

    @Value("classpath:sql/prepare_tables.sql")
    private Resource sqlScript;

    public ExecuteSqlScriptTasklet(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(sqlScript);

        // Options utiles pour les scripts complexes / Oracle :
        populator.setContinueOnError(false); // Arrête le job si une erreur SQL survient
        populator.setSeparator(";");          // Séparateur d'instructions (défaut: ';')
        populator.setBlockCommentStartDelimiter("/*");
        populator.setBlockCommentEndDelimiter("*/");

        // Exécution du script sur la base de données configurée
        populator.execute(dataSource);

        return RepeatStatus.FINISHED;
    }
}