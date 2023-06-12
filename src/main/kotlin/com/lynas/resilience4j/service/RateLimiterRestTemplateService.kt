package com.lynas.resilience4j.service

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Service
class RateLimiterRestTemplateService(
    private val restTemplate: RestTemplate
) {

    fun makeRequestRestTemplate() {

        val config = RateLimiterConfig.custom()
            .limitForPeriod(1)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofSeconds(1))
            .build()

        val registry = RateLimiterRegistry.of(config)
        val limiter = registry.rateLimiter("externalApiService")

        val externalApiSupplier =
            RateLimiter.decorateSupplier(limiter,
                { restTemplate.getForObject("http://localhost:8070/demo", String::class.java) })

        for (i in 1..20) {
            println(i)
            println(externalApiSupplier.get())
        }
    }



}
