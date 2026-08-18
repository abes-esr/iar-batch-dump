package fr.abes.sudoc.iarbatchdump.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class PpnReaderTest {

    @Autowired
    @Qualifier("oracleDataSource")
    private DataSource oracleDataSource;

    @Test
    void testPpnQueryReturnsResults() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(oracleDataSource);

        String exportAction = "update";
        int nbJours = 30;

        String whereClause = """
                b.biblevel = 'a'
                  AND b.typecontrol = 'm'
                  AND EXISTS (SELECT 1 FROM BIBLIO_TABLE_LIEN_RAMEAU WHERE ppn = b.ppn)
                  AND EXISTS (SELECT 1 FROM biblio_table_frbr_3XX WHERE tag = '330$a' AND ppn = b.ppn)
                """;

        if ("update".equals(exportAction)) {
            whereClause += " AND b.ppn IN (SELECT DISTINCT ppn FROM BIBLIO_TABLE_CHANGE_BY_TAG WHERE DATE_ETAT > SYSDATE - :nbJours AND TAG = '606$2')";
        }

        String sql = "SELECT DISTINCT b.ppn FROM biblio_table_generale b WHERE " + whereClause;

        // Exécute la requête avec JdbcTemplate
        List<String> ppnList = jdbcTemplate.queryForList(sql, String.class);

        // Vérifie que des résultats ont été retournés
        assertFalse(ppnList.isEmpty(), "La requête doit retourner au moins un PPN.");
        System.out.println("PPNs lus : " + ppnList);
    }
}