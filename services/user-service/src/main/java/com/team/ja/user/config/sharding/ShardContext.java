package com.team.ja.user.config.sharding;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShardContext {

    public static final String DEFAULT_SHARD = "user_shard_others";

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    /**
     * Sets the shard key for the current thread.
     * 
     * @param shardKey
     */
    public static void setShardKey(String shardKey) {
        log.debug("Setting shard key: {}", shardKey);
        CONTEXT.set(shardKey);
    }

    /**
     * Gets the shard key for the current thread.
     * If not set, returns the default shard key.
     * 
     * @return shard key
     */
    public static String getShardKey() {
        String shardKey = CONTEXT.get();
        if (shardKey == null) {
            shardKey = DEFAULT_SHARD;
            CONTEXT.set(shardKey);
        }
        log.debug("Getting shard key: {}", shardKey);
        return shardKey;
    }

    /**
     * Clears the shard key for the current thread.
     */
    public static void clear() {
        log.debug("Clearing shard key");
        CONTEXT.remove();
    }

    /**
     * Checks if the shard key is set for the current thread.
     * 
     * @return true if shard key is set, false otherwise
     */
    public static boolean isShardKeySet() {
        return CONTEXT.get() != null;
    }
}