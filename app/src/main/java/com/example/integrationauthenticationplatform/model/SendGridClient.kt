package com.example.integrationauthenticationplatform.model

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.*

object SendGridClient {
    private val http = HttpClient(Android)

    // Simple “is this key valid?” probe
    suspend fun validate(apiKey: String): Boolean = try {
        val resp = http.get("https://api.sendgrid.com/v3/user/account") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }
        resp.status.value in 200..299
    } catch (_: Exception) { false }
}
