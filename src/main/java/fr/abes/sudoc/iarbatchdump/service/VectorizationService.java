package fr.abes.sudoc.iarbatchdump.service;

import fr.abes.sudoc.iarbatchdump.model.RameauExportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VectorizationService {

    @Autowired
    private RestTemplate restTemplate;

    public void launchVectorization(RameauExportParams params) {
        String url = String.format(
                "http://152.228.161.151:8100/lanceVectorisation?action=%s&conceptsORchains=%s&alias_model=%s&avec_these=%s",
                params.getAction(),
                params.getConceptsORchains(),
                params.getAliasModel(),
                params.getAvecThese()
        );

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Échec du lancement de la vectorisation: " + response.getBody());
        }
    }
}