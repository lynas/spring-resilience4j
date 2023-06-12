package com.lynas.resilience4j.config

import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.*
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate1(rateLimiterRegistry: RateLimiterRegistry): RestTemplate? {
        val restTemplate = RestTemplate(
            BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
        )
        restTemplate.interceptors = listOf<ClientHttpRequestInterceptor>(RateLimiterInterceptor(rateLimiterRegistry))
        return restTemplate
    }
}

class RateLimiterInterceptor(private val rateLimiterRegistry: RateLimiterRegistry) : ClientHttpRequestInterceptor {


    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        return rateLimiterRegistry.rateLimiter("demoApi").executeSupplier { execution.execute(request, body) }
    }
}

