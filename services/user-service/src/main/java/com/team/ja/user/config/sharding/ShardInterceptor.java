package com.team.ja.user.config.sharding;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShardInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        String countryHeader = request.getHeader("X-User-Country");
        log.info("Extracted country from header: {}", countryHeader);
        if (countryHeader != null && !countryHeader.isEmpty()) {
            // Determine shard ID based on country
            String shardId = ShardingProperties.resolveShard(countryHeader);
            log.info("Routing to shard ID: {} for country: {}", shardId, countryHeader);
            ShardContext.setShardKey(shardId);
        } else {
            log.warn("No country information found in request headers.");
            ShardContext.setShardKey(ShardingProperties.DEFAULT_SHARD);
            log.info("Defaulting to shard ID: {}", ShardingProperties.DEFAULT_SHARD);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ShardContext.clear();
        log.info("Cleared shard context after request completion.");
    }

}
