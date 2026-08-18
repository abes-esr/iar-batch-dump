package fr.abes.sudoc.iarbatchdump.reader;

import fr.abes.sudoc.iarbatchdump.model.RameauExportParams;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Iterator;
import java.util.List;

public class RameauPpnReader implements ItemReader<String> {

    private JdbcTemplate jdbcTemplate;
    private Iterator<String> ppnIterator;
    private RameauExportParams params;

    @Override
    public String read() {
        if (ppnIterator == null) {
            List<String> ppns = fetchPpns();
            ppnIterator = ppns.iterator();
        }
        return ppnIterator.hasNext() ? ppnIterator.next() : null;
    }

    private List<String> fetchPpns() {
        String sql = buildPpnQuery();
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private String buildPpnQuery() {
        String baseQuery = """
            SELECT DISTINCT b.ppn
            FROM biblio_table_generale b
            WHERE b.biblevel = 'a'
              AND b.typecontrol = 'm'
              AND EXISTS (SELECT 1 FROM BIBLIO_TABLE_LIEN_RAMEAU WHERE ppn = b.ppn)
              AND EXISTS (SELECT 1 FROM biblio_table_frbr_3XX WHERE tag = '330$a' AND ppn = b.ppn)
            """;

        if ("update".equals(params.getExportAction())) {
            baseQuery += """
                AND b.ppn IN (
                    SELECT DISTINCT ppn
                    FROM BIBLIO_TABLE_CHANGE_BY_TAG
                    WHERE DATE_ETAT > SYSDATE - :nbJours
                      AND TAG = '606$2'
                )
                """;
        }

        return baseQuery;
    }

    public void setParams(RameauExportParams params) {
        this.params = params;
    }
}