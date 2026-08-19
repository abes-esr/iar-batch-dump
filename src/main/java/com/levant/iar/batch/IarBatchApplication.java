package com.levant.iar.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for IAR Batch Dump project.
 * This project handles extraction, upload, and vectorization of Rameau data.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.levant.iar.batch")
@EntityScan(basePackages = "com.levant.iar.batch")
public class IarBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(IarBatchApplication.class, args);
    }
}
