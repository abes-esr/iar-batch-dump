package com.levant.iar.batch.config;

import com.levant.iar.batch.model.PpnData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration pour vérifier que le reader peut se connecter à la base de données,
 * exécuter la requête SQL et récupérer les données PPN.
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/test-data.sql")
class BatchConfigReaderTest {

    @Autowired
    @Qualifier("oracleDataSource")
    private DataSource oracleDataSource;

    private JdbcCursorItemReader<PpnData> reader;

    @BeforeEach
    void setup() throws IOException {
        // Créer le reader manuellement avec la même logique que dans BatchConfig
        String sqlQuery = loadSqlQuery("sql/queries/select_ppn_with_rameau.sql");
        
        reader = new JdbcCursorItemReaderBuilder<PpnData>()
                .name("ppnItemReader")
                .dataSource(oracleDataSource)
                .sql(sqlQuery)
                .rowMapper(new PpnDataRowMapper())
                .verifyCursorPosition(true)
                .build();
    }

    @Test
    void testReader_ConnectsAndReadsData() throws Exception {
        // Ouvrir le reader avec un contexte d'exécution
        reader.open(new ExecutionContext());
        
        // Lire toutes les données
        List<PpnData> items = new ArrayList<>();
        PpnData item;
        
        while ((item = reader.read()) != null) {
            items.add(item);
        }
        
        // Fermer le reader
        reader.close();
        
        // Vérifications
        assertThat(items).isNotEmpty()
                .hasSizeGreaterThan(0)
                .allMatch(ppnData -> ppnData.getPpn() != null);
        
        // Vérifier que les données sont correctement mappées
        PpnData firstItem = items.get(0);
        assertThat(firstItem.getPpn()).isNotNull();
        assertThat(firstItem.getTitre()).isNotNull();
        
        System.out.println("✓ Test réussi : " + items.size() + " enregistrements lus");
        System.out.println("✓ Premier PPN : " + firstItem.getPpn());
    }

    /**
     * Charge la requête SQL depuis le fichier.
     */
    private String loadSqlQuery(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * RowMapper pour PpnData.
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
}
