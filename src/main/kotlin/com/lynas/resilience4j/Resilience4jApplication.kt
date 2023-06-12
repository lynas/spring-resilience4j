package com.lynas.resilience4j

import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@SpringBootApplication
class Resilience4jApplication {

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

fun main(args: Array<String>) {
    runApplication<Resilience4jApplication>(*args)
}

@RestController
class HomeController(
    val service: RateLimiterService
) {

    @GetMapping("/demo-base")
    suspend fun demo(): String {
        service.makeRequest()
        return "demo called"
    }
}

@Service
@RateLimiter(name = "demoApi")
class RateLimiterService(
    private val webClient: WebClient
) {

    suspend fun makeRequest() {
        println("making post call")
        for (i in 1..20) {
            val res = webClient.get(
                uri = "http://localhost:8070/demo",
                responseClass = String::class.java
            )
            println("Data $i = $res")
        }
    }


}

suspend fun <Res> WebClient.get(
    uri: String,
    responseClass: Class<Res>
): Res? = this.get()
    .uri(uri)
    .retrieve()
    .bodyToMono(responseClass)
    .awaitSingle()
