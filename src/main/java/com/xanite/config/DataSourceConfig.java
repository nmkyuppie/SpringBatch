package com.xanite.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Database configuration.
 *
 * @author acanales
 */
@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.xanite.dto")
@EnableJpaRepositories(basePackages = "com.xanite.repository")
public class DataSourceConfig {


	/**
	 * Package to scan
	 */
	private static final String DTO = "com.xanite.dto";

	@Autowired
	Environment env;

	@Primary
	@Bean
	public HikariDataSource dataSourceBatch(DataSourceProperties properties) {
		HikariDataSource ds = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		ds.setConnectionTestQuery("SELECT 1");
		return ds;

	}

	@Bean
	@ConfigurationProperties("spring.datasource")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSourceProperties properties) {
		// JpaVendorAdapteradapter can be autowired as well if it's configured in application properties.
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(false);
		// vendorAdapter.setShowSql(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		//Add package to scan for entities.
		factory.setPackagesToScan(DTO);
		factory.setDataSource(dataSourceBatch(properties));
		factory.setJpaProperties(properties());
		return factory;
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory);
		return txManager;
	}
	
	private Properties properties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.generate_statistics", env.getProperty("spring.jpa.properties.hibernate.generate_statistics"));
		properties.setProperty("hibernate.jdbc.batch_size", env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size"));
		properties.setProperty("hibernate.format_sql", env.getProperty("spring.jpa.properties.hibernate.format_sql"));
		return properties;
	}

}
