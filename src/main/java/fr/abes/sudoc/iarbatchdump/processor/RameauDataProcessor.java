package fr.abes.sudoc.iarbatchdump.processor;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RameauDataProcessor implements ItemProcessor<String, CsvRecord> {

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public RameauDataProcessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CsvRecord process(String ppn) throws Exception {
        return CsvRecord.builder()
                .ppn(ppn)
                .these(getThese(ppn))
                .titre(getTitre(ppn))
                .resume(getResume(ppn))
                .libelleRameau(getRameau(ppn))
                .langue(getLangue(ppn))
                .build();
    }

    private String getThese(String ppn) {
        return cache.computeIfAbsent("these_" + ppn, k ->
                jdbcTemplate.queryForObject(
                        """
                        SELECT ppn
                        FROM biblio_table_frbr_1XX
                        WHERE ppn = ? AND tag = '105$a'
                          AND (substr(datas, 4, 4) LIKE '%v%' OR substr(datas, 4, 4) LIKE '%m%' OR substr(datas, 4, 4) LIKE '%7%')
                          AND rownum < 2
                        """,
                        String.class, ppn
                )
        );
    }

    private String getTitre(String ppn) {
        return cache.computeIfAbsent("titre_" + ppn, k ->
                jdbcTemplate.queryForObject(
                        """
                        SELECT LISTAGG(REPLACE(REPLACE(datas, '?', ''), '?', ''), ' : ')
                        WITHIN GROUP (ORDER BY posfield, possubfield)
                        FROM BIBLIO_TABLE_FRBR_2XX
                        WHERE ppn = ? AND tag IN ('200$a', '200$e') AND ROWNUM < 15
                        """,
                        String.class, ppn
                )
        );
    }

    private String getResume(String ppn) {
        return cache.computeIfAbsent("resume_" + ppn, k ->
                jdbcTemplate.queryForObject(
                        """
                        SELECT RTRIM(
                            XMLAGG(XMLELEMENT(e, datas, ' ____ ').EXTRACT('//text()') ORDER BY posfield, possubfield).GETCLOBVAL(),
                            ': '
                        )
                        FROM BIBLIO_TABLE_FRBR_3XX
                        WHERE ppn = ? AND tag = '330$a'
                        """,
                        String.class, ppn
                )
        );
    }

    private String getRameau(String ppn) {
        return cache.computeIfAbsent("rameau_" + ppn, k -> {
            List<String> rameaux = jdbcTemplate.queryForList(
                    """
                    SELECT LISTAGG(ram, ' -- ') WITHIN GROUP (ORDER BY ppn)
                    FROM (
                        SELECT zz.ppn, zz2.datas || '#' || zz.datas AS ram, zz.posfield
                        FROM BIBLIO_TABLE_FRBR_EXTEND zz,
                             BIBLIO_TABLE_FRBR_EXTEND zz2,
                             (SELECT ppn, posfield FROM qe_rameau_all_1 WHERE ppn = ?) t
                        WHERE zz.ppn = t.ppn
                          AND zz.posfield = t.posfield
                          AND zz.tag IN ('606$a', '606$x')
                          AND zz2.ppn = t.ppn
                          AND zz2.posfield = t.posfield
                          AND zz.POSSUBFIELD - 1 = zz2.POSSUBFIELD
                          AND zz2.tag IN ('606$3')
                    )
                    GROUP BY ppn, posfield
                    """,
                    String.class, ppn
            );
            return rameaux != null ? String.join(";", rameaux) : null;
        });
    }

    private String getLangue(String ppn) {
        return cache.computeIfAbsent("langue_" + ppn, k ->
                jdbcTemplate.queryForObject(
                        """
                        SELECT LISTAGG(datas, ' : ') WITHIN GROUP (ORDER BY posfield, possubfield)
                        FROM BIBLIO_TABLE_FRBR_1XX
                        WHERE ppn = ? AND tag = '101$a'
                        """,
                        String.class, ppn
                )
        );
    }
}
