package fr.abes.sudoc.iarbatchdump.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class FileUploadService {

    @Autowired
    private RestTemplate restTemplate;

    public void uploadFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Fichier introuvable: " + filePath);
        }

        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        String url = "http://152.228.161.151:8100/uploadfile";
        ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Échec de l'upload: " + response.getBody());
        }
    }
}