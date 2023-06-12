package com.lynas.resilience4j.service

import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@RateLimiter(name = "externalApiService")
@Service
class RateLimiterRestTemplateService(
    private val restTemplate: RestTemplate
) {

    fun makeRequestRestTemplate() {
        for (i in 1..20) {
            val responese = restTemplate.getForObject("http://localhost:8070/demo", String::class.java)
            println(i)
            println(responese)
        }
    }
}
