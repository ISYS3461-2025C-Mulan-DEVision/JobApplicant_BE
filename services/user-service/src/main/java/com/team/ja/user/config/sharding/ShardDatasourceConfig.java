package com.team.ja.user.config.sharding;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.team.ja.user.repository", entityManagerFactoryRef = "shardEntityManagerFactory", transactionManagerRef = "shardTransactionManager")
public class ShardDatasourceConfig {

    private final ShardingProperties shardingProperties;

    @Bean(name = "resolvedDataSources")
    public Map<Object, Object> resolvedDataSources() {
        log.info("Building shard datasource map from ShardingProperties");

        Map<Object, Object> targetDatasources = new HashMap<>();
        Map<String, ShardingProperties.ShardProperties> shards = shardingProperties.getShards();

        if (shards == null || shards.isEmpty()) {
            throw new IllegalStateException("No shards configured in ShardingProperties");
        }

        shards.forEach((shardId, shardProps) -> {
            log.info("Creating HikariDataSource for shard: {}", shardId);
            targetDatasources.put(shardId, createHikariDataSource(shardProps));
        });

        return targetDatasources;
    }

    @Bean
    @Primary
    public DataSource shardDataSource() {

        log.info("Starting shard datasource configuration");

        if (shardingProperties == null) {
            throw new IllegalStateException("ShardingProperties bean is not available");
        }

        Map<String, ShardingProperties.ShardProperties> shards = shardingProperties.getShards();

        if (shards == null || shards.isEmpty()) {
            throw new IllegalStateException("No shards configured in ShardingProperties.shards");
        }

        ShardRoutingDataSource routingDataSource = new ShardRoutingDataSource();
        Map<Object, Object> targetDatasources = new HashMap<>();

        log.info("Configured shard ids: {}", shards.keySet());

        shards.forEach((shardId, shardProps) -> {
            log.info("Configuring datasource for shard: {}", shardId);

            // Validate required shard properties
            if (shardProps == null) {
                throw new IllegalStateException("Shard properties for '" + shardId + "' are missing");
            }
            if (shardProps.getUrl() == null || shardProps.getUrl().trim().isEmpty()) {
                throw new IllegalStateException("Shard '" + shardId + "' has empty url");
            }
            if (shardProps.getUsername() == null || shardProps.getUsername().trim().isEmpty()) {
                log.warn("Shard '{}' does not define username; proceeding but authentication may fail", shardId);
            }

            HikariDataSource dataSource = null;
            try {
                dataSource = createHikariDataSource(shardProps);
            } catch (Exception ex) {
                log.error("Failed to create datasource for shard {}: {}", shardId, ex.getMessage(), ex);
                throw ex;
            }

            log.info("Datasource configured for shard: {} with URL: {} and poolName: {}",
                    shardId, shardProps.getUrl(), dataSource.getPoolName());

            targetDatasources.put(shardId, dataSource);
        });

        routingDataSource.setTargetDataSources(targetDatasources);

        String defaultShardId = shardingProperties.getDefaultShard();
        log.info("Default shard configured as: {}", defaultShardId);

        if (defaultShardId == null || !targetDatasources.containsKey(defaultShardId)) {
            throw new IllegalStateException("Default shard ID '" + defaultShardId
                    + "' is not present in configured shards: " + targetDatasources.keySet());
        }

        DataSource defaultDataSource = (DataSource) targetDatasources.get(defaultShardId);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        routingDataSource.afterPropertiesSet();

        log.info("Shard datasource construction complete. Total shards: {}", targetDatasources.size());

        return routingDataSource;
    }

    private HikariDataSource createHikariDataSource(ShardingProperties.ShardProperties shardProps) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(shardProps.getUrl());
        dataSource.setUsername(shardProps.getUsername());
        dataSource.setPassword(shardProps.getPassword());
        dataSource.setDriverClassName(shardProps.getDriverClassName());
        dataSource.setMaximumPoolSize(shardProps.getMaximumPoolSize());
        dataSource.setMinimumIdle(shardProps.getMinimumIdle());
        dataSource.setConnectionTimeout(shardProps.getConnectionTimeout());
        dataSource.setIdleTimeout(shardProps.getIdleTimeout());
        dataSource.setMaxLifetime(shardProps.getMaxLifetime());

        log.debug("Created HikariDataSource (poolName={}, maxPool={}, minIdle={}) for URL={}",
                dataSource.getPoolName(), shardProps.getMaximumPoolSize(), shardProps.getMinimumIdle(),
                shardProps.getUrl());

        return dataSource;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean shardEntityManagerFactory(EntityManagerFactoryBuilder builder,
            @Qualifier("shardDataSource") DataSource shardDataSource) {

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "none");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.format_sql", "true");

        return builder
                .dataSource(shardDataSource)
                .packages("com.team.ja.user.model")
                .properties(jpaProperties)
                .build();

    }

    @Bean
    @Primary
    public PlatformTransactionManager shardTransactionManager(
            @Qualifier("shardEntityManagerFactory") LocalContainerEntityManagerFactoryBean shardEntityManagerFactory) {
        return new JpaTransactionManager(shardEntityManagerFactory.getObject());
    }

}
