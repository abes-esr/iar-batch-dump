package fr.abes.sudoc.iarbatchdump.partitioner;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
public class SQLqueryPartitioner {

    private final JdbcTemplate jdbcTemplate;

    public SQLqueryPartitioner(@Qualifier("oracleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Value("${app.batch.partition.chunk-size}")
    private long tailleLot;

    @Value("${app.batch.partition.source-table}")
    private String sourceTable;
    

    // partitionner, pour exécuter plusieurs requêtes en chunk
    @Bean
    public Partitioner noticePartitioner() {
        return gridSize -> {

            Map<String, ExecutionContext> partitions = new HashMap<>();


            long maxId = getMaxId();
            long countId = getCountId();

            validateIdRange(maxId, countId);

            
            int partitionNumber = 0;

            for (long borneInf = 0;
                borneInf < maxId;
                borneInf += tailleLot) {

                long borneSup = Math.min(
                        borneInf + tailleLot,
                        maxId
                );

                ExecutionContext context = new ExecutionContext();

                context.putLong("borneInf", borneInf);
                context.putLong("borneSup", borneSup);

                partitions.put(
                        "partition-" + partitionNumber,
                        context
                );

                partitionNumber++;
            }

            return partitions;
        };
    }


     private long getMaxId() {
        return jdbcTemplate.queryForObject(
            "SELECT MAX(id) FROM " + sourceTable,
            Long.class
        );
    }

    private long getCountId() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(id) FROM " + sourceTable,
            Long.class
        );
    }

    private void validateIdRange(long maxId, long countId) {
        if (maxId > countId + 1_000_000L && countId > tailleLot) {
            throw new IllegalStateException(
                "Il y a " + countId + " lignes, mais l'ID max est : " + maxId + ". Les données sont trop éparses (peut-être un problème d'attribution des IDs ?)"
            );
        }
    }
}
