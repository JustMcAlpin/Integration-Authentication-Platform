// AuthActivity.kt
package com.example.integrationauthenticationplatform

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenResponse

class AuthActivity : ComponentActivity() {

    private lateinit var groupName: String
    private lateinit var clientId: String
    private lateinit var redirectUri: String
    private lateinit var authEndpoint: String
    private lateinit var tokenEndpoint: String
    private lateinit var scopes: List<String>
    private var started = false
    private lateinit var authService: AuthorizationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authService = AuthorizationService(this)

        // If launched by browser redirect
        if (intent?.action == Intent.ACTION_VIEW || intent?.data != null) {
            handleAuthResponse(intent)
            return
        }

        // Launched by MainActivity with config extras
        if (!started) {
            groupName    = intent.getStringExtra("group")!!
            clientId     = intent.getStringExtra("clientId")!!
            redirectUri  = intent.getStringExtra("redirectUri")!!
            authEndpoint = intent.getStringExtra("authEndpoint")!!
            tokenEndpoint= intent.getStringExtra("tokenEndpoint")!!
            scopes       = intent.getStringExtra("scopes")!!.split(" ")
            startAuth()
            started = true
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthResponse(intent)
    }

    private fun startAuth() {
        val serviceCfg = AuthorizationServiceConfiguration(
            Uri.parse(authEndpoint),
            Uri.parse(tokenEndpoint)
        )
        val req = AuthorizationRequest.Builder(
            serviceCfg,
            clientId,
            ResponseTypeValues.CODE,
            Uri.parse(redirectUri)
        ).setScopes(scopes)
            .build()

        startActivity(authService.getAuthorizationRequestIntent(req))
    }

    private fun handleAuthResponse(intent: Intent) {
        val resp: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)
        val ex: AuthorizationException? = AuthorizationException.fromIntent(intent)

        if (ex != null || resp == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        authService.performTokenRequest(resp.createTokenExchangeRequest()) { tr: TokenResponse?, tex: AuthorizationException? ->
            if (tex != null || tr == null) {
                setResult(RESULT_CANCELED)
                finish()
                return@performTokenRequest
            }

            val payload = buildString {
                append("{")
                append("\"access_token\":\"${tr.accessToken}\",")
                append("\"refresh_token\":\"${tr.refreshToken ?: ""}\",")
                append("\"expires_at\":${(tr.accessTokenExpirationTime ?: 0L)/1000},")
                append("\"scope\":\"${tr.scope ?: ""}\"")
                append("}")
            }

            val data = Intent().apply {
                putExtra("group", groupName)
                putExtra("credentialJson", payload)
            }
            setResult(RESULT_OK, data)
            finish()
        }
    }
}
