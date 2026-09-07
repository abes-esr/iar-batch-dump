package fr.abes.sudoc.iarbatchdump.partitioner;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SQLrequestPartitioner {
    

    // partitionner, pour exécuter plusieurs requêtes 
    @Bean
    public Partitioner noticePartitioner() {
        return gridSize -> {

            Map<String, ExecutionContext> partitions = new HashMap<>();


            // pour l'instant en dur, mais faudra paramétrer, j'imagine que ce serait sympa de pouvoir le faire depuis les paramètres du job
            // faudra penser à faire une requête pour récupérer l'id max, et mettre des ptites sécurités (faut pas que l'id max soit trop gros, sinon on les chunks se font dans le vent)
            long maxId = 18_000_000L;
            // long maxId = 200_000L;
            long tailleLot = 50_000L;

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
}
