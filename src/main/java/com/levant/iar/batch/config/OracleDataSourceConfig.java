package com.levant.iar.batch.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Configuration for Oracle DataSource.
 * This datasource is used to connect to the external Oracle database
 * for data extraction.
 */
@Configuration
public class OracleDataSourceConfig {

    @Value("${datasource.oracle.url}")
    private String url;

    @Value("${datasource.oracle.username}")
    private String username;

    @Value("${datasource.oracle.password}")
    private String password;

    @Value("${datasource.oracle.driver-class-name}")
    private String driverClassName;

    /**
     * Creates the Oracle DataSource bean.
     * This is NOT the primary datasource (H2 is primary for Spring Batch metadata).
     * 
     * @return configured Oracle DataSource
     */
    @Bean(name = "oracleDataSource")
    public DataSource oracleDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * Creates a transaction manager specifically for Oracle operations.
     * 
     * @return PlatformTransactionManager for Oracle
     */
    @Bean(name = "oracleTransactionManager")
    public PlatformTransactionManager oracleTransactionManager() {
        return new DataSourceTransactionManager(oracleDataSource());
    }
}
