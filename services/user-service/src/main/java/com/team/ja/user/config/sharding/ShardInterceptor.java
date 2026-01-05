package com.team.ja.user.config.sharding;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.team.ja.user.service.impl.ShardLookupService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShardInterceptor implements HandlerInterceptor {

    private final ShardLookupService shardLookupService;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        if (request.getHeader("X-User-Id") != null) {
            String shardKey = shardLookupService.findShardIdByUserId(
                    java.util.UUID.fromString(request.getHeader("X-User-Id")));
            ShardContext.setShardKey(shardKey);
            log.info("Routed to shard ID: {} based on user ID", shardKey);
        } else {
            log.warn("No X-User-Id header found; cannot determine shard.");
            log.warn("Proceeding without shard context may lead to errors.");
            ShardContext.setShardKey(ShardContext.DEFAULT_SHARD);
            log.info("Defaulted to shard ID: {}", ShardContext.DEFAULT_SHARD);
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
