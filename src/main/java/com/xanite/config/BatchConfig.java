package com.xanite.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.cache.configuration.MutableConfiguration;

@Configuration
@Slf4j
@Import(DataSourceConfig.class)
@ComponentScan(basePackages = {"com.xanite.config",
							"com.xanite.process",
							"com.xanite.dao", 
							"com.xanite.writers", 
							"com.xanite.readers", 
							"com.xanite.processors", 
							"com.xanite.utils",
							"com.xanite.listeners" })
@EnableBatchProcessing
@EnableCaching
public class BatchConfig {

    /**
     * Cache for interest rates
     */
    private static final String INTEREST_RATES_CACHE = "interestRates";

    @Bean
    public JCacheManagerCustomizer cacheConfigurationCustomizer() {
        return cm -> cm.createCache(INTEREST_RATES_CACHE, cacheConfiguration());
    }

    /**
     * Create a simple configuration that enable statistics via the JCache programmatic configuration API.
     * <p>
     * Within the configuration object that is provided by the JCache API standard, there is only a very limited set of
     * configuration options. The really relevant configuration options (like the size limit) must be set via a
     * configuration mechanism that is provided by the selected JCache implementation.
     */
    private javax.cache.configuration.Configuration<Object, Object> cacheConfiguration() {
        return new MutableConfiguration<>().setStatisticsEnabled(true);
    }

    /**
     * This bean will set the global thread pool for Spring Batch. It will use the
     * pool properties set in the properties.
     *
     * @return
     */
    @Bean
    public TaskExecutor taskExecutor(ConfigProperties prop) {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(prop.getCorePoolSize());
        taskExecutor.setMaxPoolSize(prop.getMaxPoolSize());
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    /**
     * Setting the datasource for Spring Batch. We are using an embedded database.
     * @return
     */
    @Bean
    public BatchConfigurer batchConfigurer() {
        log.debug("Creating Custom Batch Configurer");
        return new CustomBatchConfigurer();
    }

    class CustomBatchConfigurer implements BatchConfigurer {


        private PlatformTransactionManager transactionManager;
        private JobRepository jobRepository;
        private JobLauncher jobLauncher;
        private JobExplorer jobExplorer;

        protected CustomBatchConfigurer() {
        }

        @Override
        public JobRepository getJobRepository() {
            return this.jobRepository;
        }

        @Override
        public PlatformTransactionManager getTransactionManager() {
            return this.transactionManager;
        }

        @Autowired
        public void setTransactionManager(final PlatformTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }

        @Override
        public JobLauncher getJobLauncher() {
            return this.jobLauncher;
        }

        @Override
        public JobExplorer getJobExplorer() {
            return this.jobExplorer;
        }

        @PostConstruct
        public void initialize() {
            try {

                log.warn("Using a Map based JobRepository");

                if (this.transactionManager == null) {
                    this.transactionManager = new ResourcelessTransactionManager();
                }

                final MapJobRepositoryFactoryBean jobRepositoryFactory = new MapJobRepositoryFactoryBean(
                        this.transactionManager);
                jobRepositoryFactory.afterPropertiesSet();
                this.jobRepository = jobRepositoryFactory.getObject();

                final MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean(jobRepositoryFactory);
                jobExplorerFactory.afterPropertiesSet();
                this.jobExplorer = jobExplorerFactory.getObject();


                this.jobLauncher = createJobLauncher();
            } catch (final Exception e) {
                throw new BatchConfigurationException(e);
            }
        }

        private JobLauncher createJobLauncher() throws Exception {
            final SimpleJobLauncher jobLauncherNew = new SimpleJobLauncher();

            jobLauncherNew.setJobRepository(this.jobRepository);
            jobLauncherNew.afterPropertiesSet();
            return jobLauncherNew;
        }

        protected JobRepository createJobRepository() throws Exception {

            final JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
            factory.setDataSource(null);
            factory.setTransactionManager(getTransactionManager());
            factory.afterPropertiesSet();
            return factory.getObject();
        }
    }
}