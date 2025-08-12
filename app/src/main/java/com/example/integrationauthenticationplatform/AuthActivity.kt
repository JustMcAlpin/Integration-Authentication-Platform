package com.example.integrationauthenticationplatform

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

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

        // tiny spinner so it’s not a blank screen while we auth/exchange
        setContent { Surface { CircularProgressIndicator() } }

        authService = AuthorizationService(this)

        // Return from browser? Handle immediately.
        if (intent?.action == Intent.ACTION_VIEW || intent?.data != null) {
            handleAuthResponse(intent)
            return
        }

        // Launched from Main with config extras
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

    override fun onDestroy() {
        super.onDestroy()
        if (this::authService.isInitialized) authService.dispose()
    }

    private fun startAuth() {
        val serviceCfg = AuthorizationServiceConfiguration(
            Uri.parse(authEndpoint),
            Uri.parse(tokenEndpoint)
        )
        val extras = (intent.getSerializableExtra("extraParams") as? HashMap<String, String>)
            ?.toMutableMap() ?: mutableMapOf()
        val prompt = extras.remove("prompt")

        val builder = AuthorizationRequest.Builder(
            serviceCfg,
            clientId,
            ResponseTypeValues.CODE,
            Uri.parse(redirectUri)
        ).setScopes(scopes)

        if (!prompt.isNullOrBlank()) builder.setPromptValues(prompt)

        // carry which provider group we’re doing through redirects
        builder.setState(groupName)

        val req = builder.setAdditionalParameters(extras).build()
        android.util.Log.d("Auth123", "AUTH: launch -> ${req.toUri()}")
        startActivity(authService.getAuthorizationRequestIntent(req))
    }

    private fun handleAuthResponse(intent: Intent) {
        android.util.Log.d("Auth123", "AUTH: handleAuthResponse intent=${intent.dataString}")
        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        if (ex != null || resp == null) {
            android.util.Log.e("Auth123", "AUTH: error ex=$ex")
            runOnUiThread { setResult(RESULT_CANCELED); finish() }
            return
        }

        authService.performTokenRequest(resp.createTokenExchangeRequest()) { tr, tex ->
            if (tex != null || tr == null) {
                android.util.Log.e("Auth123", "Token exchange failed", tex)
                runOnUiThread { setResult(RESULT_CANCELED); finish() }
                return@performTokenRequest
            }

            android.util.Log.d("Auth123", "AUTH: token OK, exp=${tr.accessTokenExpirationTime}")

            val payload = buildString {
                append("{")
                append("\"access_token\":\"${tr.accessToken}\",")
                append("\"refresh_token\":\"${tr.refreshToken ?: ""}\",")
                append("\"expires_at\":${(tr.accessTokenExpirationTime ?: 0L) / 1000},")
                append("\"scope\":\"${tr.scope ?: ""}\"")
                append("}")
            }

            val groupFromState = resp.request?.state ?: groupName

            // hand off directly to MainActivity; finish to avoid white screen
            val handoff = Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK   // in case Main isn’t in the task stack
                )
                putExtra("group", groupFromState)
                putExtra("credentialJson", payload)
            }
            android.util.Log.d("Auth123", "AUTH: handoff -> Main group=$groupFromState")
            runOnUiThread {
                startActivity(handoff)
                // also setResult for the ActivityResult path; harmless if unused
                setResult(RESULT_OK, Intent().apply {
                    putExtra("group", groupFromState)
                    putExtra("credentialJson", payload)
                })
                finish()
            }
        }
    }
}
