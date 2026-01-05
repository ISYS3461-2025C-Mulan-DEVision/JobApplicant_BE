package com.team.ja.user.config.sharding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "sharding")
public class ShardingProperties {

    private String defaultShard = "user_shard_others";

    private Map<String, ShardProperties> shards = new HashMap<>();

    /**
     * Map of country ISO codes (upper-case) to shard id.
     * Example: { "VN":"user_shard_vn", "SG":"user_shard_sg",
     * "AU":"user_shard_oceania" }
     */
    private Map<String, String> byCountry = new HashMap<>();

    /**
     * Properties for individual shards.
     */
    @Data
    public static class ShardProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName = "org.postgresql.Driver";

        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
    }

    public static final String DEFAULT_SHARD = "user_shard_others";

    private static final Map<String, String> COUNTRY_TO_SHARD = new HashMap<>();

    static {
        // Vietnam mapped to user_shard_vn
        COUNTRY_TO_SHARD.put("VN", "user_shard_vn");

        // Singapore mapped to user_shard_sg
        COUNTRY_TO_SHARD.put("SG", "user_shard_sg");

        // Oceania countries mapped to user_shard_oceania
        List.of("AU", "NZ")
                .forEach(country -> COUNTRY_TO_SHARD.put(country, "user_shard_oceania"));

        // East Asia countries mapped to user_shard_east_asia
        List.of("JP", "KR", "CN")
                .forEach(country -> COUNTRY_TO_SHARD.put(country, "user_shard_east_asia"));

        // North America countries mapped to user_shard_north_america
        List.of("US", "CA")
                .forEach(country -> COUNTRY_TO_SHARD.put(country, "user_shard_north_america"));

        // Europe countries mapped to user_shard_europe
        List.of("GB", "FR", "DE", "NL")
                .forEach(country -> COUNTRY_TO_SHARD.put(country, "user_shard_europe"));

        // Other Countries mapped to default shard
        COUNTRY_TO_SHARD.put("DEFAULT", DEFAULT_SHARD);
    }

    public static String resolveShard(String countryIso) {
        if (countryIso == null) {
            return DEFAULT_SHARD;
        }
        String key = countryIso.trim().toUpperCase();
        return COUNTRY_TO_SHARD.getOrDefault(key, DEFAULT_SHARD);
    }

    /**
     * Gets shard id for a given country ISO code.
     */
    public String getShardForCountry(String countryIso) {
        if (countryIso == null) {
            return defaultShard;
        }
        String key = countryIso.trim().toUpperCase();
        return byCountry.getOrDefault(key, defaultShard);
    }

}
