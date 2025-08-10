package com.example.integrationauthenticationplatform.data

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*

object RevokeClient {
    private val http = HttpClient(Android)
    suspend fun revokeGoogle(token: String) {
        try { http.post("https://oauth2.googleapis.com/revoke") { parameter("token", token) } }
        catch (_: Exception) { /* best-effort; still remove locally */ }
    }
}
