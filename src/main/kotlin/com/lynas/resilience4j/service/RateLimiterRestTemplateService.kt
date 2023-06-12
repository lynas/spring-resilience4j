package com.lynas.resilience4j.service

import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@RateLimiter(name = "demoApi")
class RateLimiterRestTemplateService(
    private val restTemplate: RestTemplate
) {

    fun makeRequestRestTemplate() {
        println("making post call")
        for (i in 1..20) {
            val res2 = restTemplate.getForEntity("http://localhost:8070/demo", String::class.java).body
            println("Data $i = $res2")
        }
    }
}
