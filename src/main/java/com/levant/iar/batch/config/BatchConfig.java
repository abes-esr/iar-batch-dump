package com.levant.iar.batch.config;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Main Spring Batch configuration.
 * Configures the batch infrastructure using the primary H2 datasource
 * for storing batch metadata.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Value("${spring.batch.jdbc.initialize-schema:always}")
    private String initializeSchema;

    /**
     * Custom batch configurer that uses the primary datasource (H2).
     * 
     * @param dataSource the primary H2 datasource
     * @return BatchConfigurer
     */
    @Bean
    public BatchConfigurer batchConfigurer(DataSource dataSource) {
        return new DefaultBatchConfigurer(dataSource);
    }

    /**
     * Job repository factory bean for Spring Batch metadata.
     * 
     * @param dataSource the primary datasource
     * @param transactionManager the transaction manager
     * @return JobRepositoryFactoryBean
     */
    @Bean
    public JobRepositoryFactoryBean jobRepositoryFactory(
            DataSource dataSource,
            PlatformTransactionManager transactionManager) {
        
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        
        if ("always".equalsIgnoreCase(initializeSchema)) {
            factory.setDatabaseType("H2");
        }
        
        return factory;
    }

    /**
     * Job explorer for querying batch jobs.
     * 
     * @param dataSource the primary datasource
     * @return JobExplorer
     */
    @Bean
    public JobExplorer jobExplorer(DataSource dataSource) throws Exception {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * Job launcher for executing batch jobs.
     * 
     * @param jobRepository the job repository
     * @return JobLauncher
     */
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        return jobLauncher;
    }
}
