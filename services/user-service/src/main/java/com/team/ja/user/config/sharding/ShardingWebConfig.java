package com.team.ja.user.config.sharding;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ShardingWebConfig implements WebMvcConfigurer {

    private final ShardInterceptor shardInterceptor;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(shardInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/docs/**", "/api/swagger-ui/**");
    }
}
