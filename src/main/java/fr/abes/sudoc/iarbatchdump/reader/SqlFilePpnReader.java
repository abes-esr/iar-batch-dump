package fr.abes.sudoc.iarbatchdump.reader;

import fr.abes.sudoc.iarbatchdump.model.RameauExportParams;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * Reader qui exécute une requête SQL stockée dans un fichier externe
 * et retourne les PPN un par un.
 */
public class SqlFilePpnReader implements ItemReader<String> {

    private JdbcTemplate jdbcTemplate;
    private Resource sqlFileResource;
    private Iterator<String> ppnIterator;
    private RameauExportParams params;
    private String sqlQuery;

    /**
     * Constructeur avec injection des dépendances
     */
    public SqlFilePpnReader(JdbcTemplate jdbcTemplate, Resource sqlFileResource) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlFileResource = sqlFileResource;
    }

    @Override
    public String read() {
        if (ppnIterator == null) {
            loadSqlQuery();
            List<String> ppns = fetchPpns();
            ppnIterator = ppns.iterator();
        }
        return ppnIterator.hasNext() ? ppnIterator.next() : null;
    }

    /**
     * Charge la requête SQL depuis le fichier
     */
    private void loadSqlQuery() {
        try (Reader reader = new InputStreamReader(sqlFileResource.getInputStream(), StandardCharsets.UTF_8)) {
            this.sqlQuery = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier SQL: " + sqlFileResource.getDescription(), e);
        }
    }

    /**
     * Exécute la requête et récupère les PPN
     */
    private List<String> fetchPpns() {
        // Applique les paramètres si nécessaire
        String finalQuery = applyParameters(sqlQuery);
        return jdbcTemplate.queryForList(finalQuery, String.class);
    }

    /**
     * Applique les paramètres dynamiques à la requête
     */
    private String applyParameters(String query) {
        if (params == null) {
            return query;
        }
        
        // Pour l'instant, on gère uniquement le paramètre exportAction
        // qui détermine si on filtre sur les notices modifiées récemment
        if ("update".equals(params.getExportAction())) {
            // Ajouter la condition de filtrage pour les mises à jour
            String updateCondition = " AND b.ppn IN (SELECT DISTINCT ppn FROM BIBLIO_TABLE_CHANGE_BY_TAG WHERE DATE_ETAT > SYSDATE - " + params.getNbJours() + " AND TAG = '606$2')";
            
            // Trouver la position de la clause WHERE dans la première CTE
            int wherePos = query.indexOf("WHERE b.biblevel = 'a'");
            if (wherePos != -1) {
                // Insérer la condition avant le ROWNUM
                int rownumPos = query.indexOf("AND ROWNUM <= 1000");
                if (rownumPos != -1) {
                    return query.substring(0, rownumPos) + updateCondition + query.substring(rownumPos);
                }
            }
        }
        
        return query;
    }

    public void setParams(RameauExportParams params) {
        this.params = params;
    }

    public void setSqlFileResource(Resource sqlFileResource) {
        this.sqlFileResource = sqlFileResource;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
