package com.team.ja.user.config.sharding;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShardRoutingDataSource extends AbstractRoutingDataSource {

    // This method is called before each database query to determine which shard's
    // datasource should be used
    @Override
    protected Object determineCurrentLookupKey() {
        String shardKey = ShardContext.getShardKey();
        log.debug("Routing database operation to shard: {}", shardKey);
        return shardKey;
    }

}
