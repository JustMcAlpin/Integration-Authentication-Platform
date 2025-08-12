package com.example.integrationauthenticationplatform.model

import android.util.Base64
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.*

object TwilioClient {
    private val http = HttpClient(Android)

    suspend fun validate(accountSid: String, authToken: String): Boolean = try {
        val creds = "$accountSid:$authToken"
        val basic = "Basic " + Base64.encodeToString(creds.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val resp = http.get("https://api.twilio.com/2010-04-01/Accounts/$accountSid.json") {
            header(HttpHeaders.Authorization, basic)
        }
        resp.status.value in 200..299
    } catch (_: Exception) {
        false
    }
}
