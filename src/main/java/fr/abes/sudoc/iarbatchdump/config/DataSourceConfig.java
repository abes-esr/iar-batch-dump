package fr.abes.sudoc.iarbatchdump.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // --- DataSource Oracle SUDOC ---
    @Bean
    @Primary // j'ai mis primary sur cette data source sinon spring choisi h2 et reconnait pas les tables
    @ConfigurationProperties(prefix = "app.datasource.oracle")
    public DataSource oracleDataSource() {
        return DataSourceBuilder.create().build();
    }

    // --- DataSource H2 (pour Spring Batch) ---
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("batchdb")
                .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
                .build();
    }
}