package com.lynas.resilience4j.service

import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
@RateLimiter(name = "demoApi")
class RateLimiterWebClientService(
    private val webClient: WebClient,
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