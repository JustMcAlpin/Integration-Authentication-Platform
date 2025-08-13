package com.example.integrationauthenticationplatform.data

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.formUrlEncode

object RevokeClient {
    private val http = HttpClient(Android)

    suspend fun revokeGoogle(token: String) { /* existing */ }

    // X (Twitter): POST form { token, client_id } to /2/oauth2/revoke
    suspend fun revokeX(token: String, clientId: String) {
        try {
            http.post("https://api.x.com/2/oauth2/revoke") {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody(listOf("token" to token, "client_id" to clientId).formUrlEncode())
            }
        } catch (_: Exception) { /* best-effort */ }
    }

    // TikTok (if you add it next)
    suspend fun revokeTikTok(token: String) {
        try {
            http.post("https://open.tiktokapis.com/v2/oauth/revoke/") {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody(listOf("token" to token).formUrlEncode())
            }
        } catch (_: Exception) { }
    }
}
