package fr.abes.sudoc.iarbatchdump.reader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import fr.abes.sudoc.iarbatchdump.mapper.NoticeRowMapper;
import fr.abes.sudoc.iarbatchdump.model.CsvRecord;

@Configuration
public class NoticeReaderConfig {
    

    // reader qui lit le résultat de la procédure (la procédure doit être lancée au préalable, et les résultats sont dans la table IAR_RESULTAT_PROCEDURE)
    @Bean
    @StepScope
    public JdbcCursorItemReader<CsvRecord> noticeReaderProcedure(
            @Qualifier("testDataSource") DataSource dataSource
    ) throws IOException {
        

        String sql = "SELECT * FROM IAR_RESULTAT_PROCEDURE";
        
    
        return new JdbcCursorItemReaderBuilder<CsvRecord>()
                .name("noticeReaderProcedure")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(new NoticeRowMapper())
                .build();
    }


// reader qui lit par chunk la requête sur les vraies tables
    @Bean
    @StepScope
    @Primary
    public JdbcCursorItemReader<CsvRecord> noticeReaderRequete(
            DataSource dataSource,
            @Value("#{jobParameters['sqlFile']}") String sqlFile,
            @Value("#{jobParameters['action']}") String action,
            @Value("#{stepExecutionContext['borneInf']}") Long borneInf,
            @Value("#{stepExecutionContext['borneSup']}") Long borneSup)
    throws IOException {
        
        String sql;
        
        sql = new String(
                getClass().getResourceAsStream("/sql/" + sqlFile).readAllBytes(),
                StandardCharsets.UTF_8
        );
        
        // si on est en action --init, on enlève la clause where qui dit "prend que ceux modifiés depuis une semaine" 
        if (action != null && "init".equals(action)) {
            sql = sql.replace("${UPDATE_CONDITION}", "");
        } else {
            
            sql = sql.replace(
                "${UPDATE_CONDITION}",
                """
                AND b.ppn IN (
                    SELECT DISTINCT ppn
                    FROM BIBLIO_TABLE_CHANGE_BY_TAG
                    WHERE DATE_ETAT > SYSDATE - 7
                    AND TAG = '606$2'
                )
                """
            );
        }
        

        return new JdbcCursorItemReaderBuilder<CsvRecord>()
                .name("noticeReaderRequete")
                .dataSource(dataSource)
                .sql(sql)
                .preparedStatementSetter(null)
                .preparedStatementSetter(ps -> {
                    ps.setLong(1, borneInf);
                    ps.setLong(2, borneSup);
                })
                .rowMapper(new NoticeRowMapper())
                .build();
    }



}
