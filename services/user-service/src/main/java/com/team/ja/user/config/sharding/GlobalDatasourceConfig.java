package com.team.ja.user.config.sharding;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.team.ja.user.model.Country;
import com.team.ja.user.model.Skill;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.team.ja.user.repository.global", entityManagerFactoryRef = "globalEntityManagerFactory", transactionManagerRef = "globalTransactionManager")
public class GlobalDatasourceConfig {

    @Bean
    @ConfigurationProperties("spring.global-datasource")
    public DataSourceProperties globalDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("globalDataSource")
    public DataSource globalDataSource() {
        return globalDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean("globalEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean globalEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        log.info("Configuring Global EntityManagerFactory with global datasource");
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "none");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        return builder
                .dataSource(globalDataSource())
                .packages(Country.class, Skill.class)
                .properties(jpaProperties)
                .persistenceUnit("global")
                .build();
    }

    @Bean("globalTransactionManager")
    public PlatformTransactionManager globalTransactionManager(
            @Qualifier("globalEntityManagerFactory") LocalContainerEntityManagerFactoryBean globalEntityManagerFactory) {
        return new JpaTransactionManager(globalEntityManagerFactory.getObject());
    }
}
