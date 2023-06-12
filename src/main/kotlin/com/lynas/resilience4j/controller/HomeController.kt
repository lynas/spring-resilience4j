package com.lynas.resilience4j.controller

import com.lynas.resilience4j.service.RateLimiterWebClientService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController(
    val service: RateLimiterWebClientService
) {

    @GetMapping("/demo-base")
    suspend fun demo(): String {
        service.makeRequest()
        return "demo called"
    }
}