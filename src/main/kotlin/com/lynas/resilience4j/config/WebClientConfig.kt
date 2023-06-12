package com.lynas.resilience4j.config

import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient {

        return WebClient.builder()
            .filter { req, next ->
                next.exchange(req).transformDeferred(RateLimiterOperator.of(rateLimiter()))
            }
            .build()
    }

    @Bean
    fun rateLimiter(): io.github.resilience4j.ratelimiter.RateLimiter {
        val config = RateLimiterConfig.custom()
            .limitForPeriod(2) // make max 2 call per second
            .limitRefreshPeriod(Duration.ofSeconds(5)) // then wait for 5 second before making next 2 call
            .timeoutDuration(Duration.ofSeconds(10)) // max wait duration. if limitRefreshPeriod > timeoutDuration it will throw error
            .build();
        val rateLimiterRegistry = RateLimiterRegistry.of(config);
        return rateLimiterRegistry.rateLimiter("demoApi");
    }
}