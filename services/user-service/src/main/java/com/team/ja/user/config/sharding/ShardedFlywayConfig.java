package com.team.ja.user.config.sharding;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Slf4j
public class ShardedFlywayConfig {

    /**
     * This bean manually runs Flyway migrations for every shard.
     * 
     * @param resolvedDataSources This is injected from your ShardDatasourceConfig.
     *                            It contains the map of all 7 shard DataSources.
     */
    @Bean(name = "migrateAllShards")
    public Boolean migrateAllShards(@Qualifier("resolvedDataSources") Map<Object, Object> resolvedDataSources) {
        log.info("Starting Flyway migrations for {} shards...", resolvedDataSources.size());

        resolvedDataSources.forEach((shardId, dataSourceObj) -> {
            log.info(">>> Migrating Shard Database: {}", shardId);

            try {
                // Cast the object to a standard DataSource
                DataSource dataSource = (DataSource) dataSourceObj;

                // Configure Flyway for this specific shard
                Flyway flyway = Flyway.configure()
                        .dataSource(dataSource)
                        .locations("classpath:db/migration")
                        // creates the flyway history table if it doesn't exist
                        .baselineOnMigrate(true)
                        // allows scripts to be run if you accidentally add an older version number
                        .outOfOrder(true)
                        .load();

                // Execute the migration
                flyway.migrate();

                log.info(">>> Successfully migrated Shard: {}", shardId);
            } catch (Exception e) {
                log.error("Critical error migrating shard {}: {}", shardId, e.getMessage());
                // Rethrowing will stop the application from starting with an invalid schema
                throw new RuntimeException("Migration failed for shard " + shardId, e);
            }
        });

        log.info("All 7 shard migrations are complete.");
        return true;
    }
}