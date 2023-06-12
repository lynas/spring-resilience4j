package com.lynas.resilience4j.config

import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration
import io.github.resilience4j.ratelimiter.RateLimiter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse


class RateLimitingInterceptor(private val limiter: RateLimiter) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val rateLimitedRequest = RateLimiter.decorateSupplier(limiter) { execution.execute(request, body) }
        return rateLimitedRequest.get()
    }
}


@Configuration
class RestTemplateConfig {

    @Bean
    @Qualifier("restTemplateProgrammatic")
    fun restTemplate(): RestTemplate {
        val config = RateLimiterConfig.custom()
            .limitForPeriod(2)
            .limitRefreshPeriod(Duration.ofSeconds(2))
            .timeoutDuration(Duration.ofSeconds(5))
            .build()

        val registry = RateLimiterRegistry.of(config)
        val limiter = registry.rateLimiter("externalApiService")

        return RestTemplateBuilder()
            .interceptors(RateLimitingInterceptor(limiter))
            .build()
    }

    @Bean
    @Qualifier("restTemplateWithYml")
    fun restTemplateWithYml(rateLimiterRegistry: RateLimiterRegistry): RestTemplate? {
        val limiter = rateLimiterRegistry.rateLimiter("externalApiService")

        return RestTemplateBuilder()
            .interceptors(RateLimitingInterceptor(limiter))
            .build()
    }
}
