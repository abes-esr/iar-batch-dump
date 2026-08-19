package com.levant.iar.batch.reader;

import com.levant.iar.batch.model.PpnData;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Configuration for PPN (Point d'accès Public Normalisé) reader.
 * Uses JdbcCursorItemReader to read PPN data from Oracle database using a SQL query
 * loaded from a file.
 */
@Configuration
public class PpnReaderConfig {

    @Value("${app.batch.ppn-query-file:sql/queries/select_ppn_with_rameau.sql}")
    private String queryFilePath;

    /**
     * RowMapper for mapping ResultSet to PpnData objects.
     */
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

    /**
     * Loads SQL query from the specified file path.
     * 
     * @param filePath the path to the SQL file
     * @return the SQL query as String
     * @throws IOException if the file cannot be read
     */
    private String loadSqlQuery(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Creates a JdbcCursorItemReader for reading PPN data from Oracle.
     * The SQL query is loaded from the configured file path.
     * 
     * @param oracleDataSource the Oracle datasource
     * @return configured JdbcCursorItemReader<PpnData>
     */
    @Bean
    public JdbcCursorItemReader<PpnData> ppnItemReader(
            @Qualifier("oracleDataSource") DataSource oracleDataSource) throws IOException {
        
        String sqlQuery = loadSqlQuery(queryFilePath);
        
        return new JdbcCursorItemReaderBuilder<PpnData>()
                .name("ppnItemReader")
                .dataSource(oracleDataSource)
                .sql(sqlQuery)
                .rowMapper(new PpnDataRowMapper())
                .verifyCursorPosition(true)
                .driverSupportsAbsolute(true)
                .build();
    }
}
