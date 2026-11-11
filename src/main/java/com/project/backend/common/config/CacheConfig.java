package com.project.backend.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats()
        );
        cacheManager.setCacheNames(java.util.List.of(
                "questionById",
                "questionBySlug",
                "publishedQuestionById",
                "publishedQuestionBySlug",
                "questionsByIds",
                "publishedQuestionsByIds",
                "executionQuestion",
                "executionTestCases",
                "executionLanguages",
                "languageById",
                "languageByJudge0Id"
        ));
        return cacheManager;
    }
}