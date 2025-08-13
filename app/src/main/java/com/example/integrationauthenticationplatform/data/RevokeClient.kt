package com.example.integrationauthenticationplatform.data

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode

object RevokeClient {
    private val http = HttpClient(Android)

    suspend fun revokeGoogle(token: String) {
        try { http.post("https://oauth2.googleapis.com/revoke") { parameter("token", token) } }
        catch (_: Exception) {}
    }

    suspend fun revokeX(token: String, clientId: String) {
        try {
            http.post("https://api.x.com/2/oauth2/revoke") {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody(
                    Parameters.build {
                        append("token", token)
                        append("client_id", clientId)
                    }.formUrlEncode()
                )
            }
        } catch (_: Exception) {}
    }
}
