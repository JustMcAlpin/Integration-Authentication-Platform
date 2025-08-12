package com.example.integrationauthenticationplatform

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import java.security.SecureRandom

class AuthActivity : ComponentActivity() {

    private lateinit var groupName: String
    private lateinit var clientId: String
    private lateinit var redirectUri: String
    private lateinit var authEndpoint: String
    private lateinit var tokenEndpoint: String
    private lateinit var scopes: List<String>

    private lateinit var authService: AuthorizationService
    private val prefs by lazy { getSharedPreferences("auth_pkce", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // lil spinner so it’s not a blank screen
        setContent { Surface { CircularProgressIndicator() } }

        authService = AuthorizationService(this)

        // If launched by browser redirect, handle immediately
        if (intent?.action == Intent.ACTION_VIEW || intent?.data != null) {
            handleRedirectFromBrowser(intent)
            return
        }

        // Launched from Main with config extras
        groupName    = intent.getStringExtra("group")!!
        clientId     = intent.getStringExtra("clientId")!!
        redirectUri  = intent.getStringExtra("redirectUri")!!
        authEndpoint = intent.getStringExtra("authEndpoint")!!
        tokenEndpoint= intent.getStringExtra("tokenEndpoint")!!
        scopes       = intent.getStringExtra("scopes")!!.split(" ")

        startAuth()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_VIEW || intent.data != null) {
            handleRedirectFromBrowser(intent)
        }
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

        // PKCE: generate + stash verifier tied to state (group)
        val codeVerifier = generateCodeVerifier()
        prefs.edit().putString("pkce_verifier_$groupName", codeVerifier).apply()

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
        builder.setState(groupName)
        builder.setCodeVerifier(codeVerifier)

        val req = builder.setAdditionalParameters(extras).build()
        android.util.Log.d("Auth123", "AUTH: launch -> ${req.toUri()}")
        startActivity(authService.getAuthorizationRequestIntent(req))
    }

    private fun handleRedirectFromBrowser(intent: Intent) {
        val uri = intent.data
        android.util.Log.d("Auth123", "AUTH: handleAuthResponse intent=${uri?.toString()}")

        if (uri == null) { finishWithCanceled(); return }

        val code  = uri.getQueryParameter("code") ?: run { finishWithCanceled(); return }
        val state = uri.getQueryParameter("state") ?: groupName

        val verifier = prefs.getString("pkce_verifier_$state", null)
        if (verifier.isNullOrBlank()) {
            android.util.Log.e("Auth123", "AUTH: missing PKCE verifier for state=$state")
            finishWithCanceled()
            return
        }

        val serviceCfg = AuthorizationServiceConfiguration(
            Uri.parse(authEndpoint),
            Uri.parse(tokenEndpoint)
        )

        val tokenReq = TokenRequest.Builder(serviceCfg, clientId)
            .setAuthorizationCode(code)
            .setRedirectUri(Uri.parse(redirectUri))
            .setCodeVerifier(verifier)
            .build()

        authService.performTokenRequest(tokenReq) { tr, tex ->
            if (tex != null || tr == null) {
                android.util.Log.e("Auth123", "AUTH: token exchange failed", tex)
                finishWithCanceled()
                return@performTokenRequest
            }

            android.util.Log.d("Auth123", "AUTH: token OK, exp=${tr.accessTokenExpirationTime}")

            val payload = """
                {
                  "access_token":"${tr.accessToken}",
                  "refresh_token":"${tr.refreshToken ?: ""}",
                  "expires_at":${(tr.accessTokenExpirationTime ?: 0L) / 1000},
                  "scope":"${tr.scope ?: ""}"
                }
            """.trimIndent()

            // ✅ Return to Main via ActivityResult
            val result = Intent().apply {
                putExtra("group", state)
                putExtra("credentialJson", payload)
            }
            setResult(RESULT_OK, result)
            finish()
        }
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun finishWithCanceled() {
        setResult(RESULT_CANCELED); finish()
    }
}
