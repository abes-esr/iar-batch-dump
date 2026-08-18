package fr.abes.sudoc.iarbatchdump.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // --- DataSource Oracle SUDOC ---
    @Bean
    @ConfigurationProperties(prefix = "app.datasource.oracle")
    public DataSource oracleDataSource() {
        return DataSourceBuilder.create().build();
    }

    // --- DataSource H2 (pour Spring Batch) ---
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.h2")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:batchdb;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .build();
    }
}