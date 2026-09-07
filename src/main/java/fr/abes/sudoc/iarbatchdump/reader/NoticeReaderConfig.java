package fr.abes.sudoc.iarbatchdump.reader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import fr.abes.sudoc.iarbatchdump.mapper.NoticeRowMapper;
import fr.abes.sudoc.iarbatchdump.model.CsvRecord;

@Configuration
public class NoticeReaderConfig {
    

    // reader qui lit le résultat de la procédure (la procédure a été lancée à la main et les résultats sont dans la table IAR_RESULTAT_ORIGINAL)
    @Bean
    @StepScope
    public JdbcCursorItemReader<CsvRecord> noticeReaderProcedure(
            DataSource dataSource
    ) throws IOException {
        

        String sql = "SELECT * FROM IAR_RESULTAT_ORIGINAL";
        
    
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
            @Value("classpath:/sql/notices_query.sql") Resource sqlFile,
            @Value("#{stepExecutionContext['borneInf']}") Long borneInf,
            @Value("#{stepExecutionContext['borneInf']}") Long borneSup)
    throws IOException {
        
        String sql;
        
        sql = new String(
                sqlFile.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
        

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
