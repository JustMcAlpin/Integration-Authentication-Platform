package com.example.integrationauthenticationplatform

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import android.util.Base64
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.openid.appauth.AuthorizationException
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
    private val http by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Surface { CircularProgressIndicator() } }
        authService = AuthorizationService(this)

        // If launched by redirect (VIEW intent), handle it
        if (intent?.action == Intent.ACTION_VIEW || intent?.data != null) {
            handleRedirectFromBrowser(intent)
            return
        }

        // Launched fresh from Main with config extras
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
        // just in case a browser re-delivers; handle again
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

        // generate + stash our own PKCE verifier
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
        builder.setCodeVerifier(codeVerifier) // important

        val req = builder.setAdditionalParameters(extras).build()
        android.util.Log.d("Auth123", "AUTH: launch -> ${req.toUri()}")
        startActivity(AuthorizationService(this).getAuthorizationRequestIntent(req))
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
            finishWithCanceled(); return
        }

        val serviceCfg = AuthorizationServiceConfiguration(
            Uri.parse(authEndpoint),
            Uri.parse(tokenEndpoint)
        )

        val tokenReq = net.openid.appauth.TokenRequest.Builder(serviceCfg, clientId)
            .setAuthorizationCode(code)
            .setRedirectUri(Uri.parse(redirectUri))   // must exactly match auth request
            .setCodeVerifier(verifier)
            .build()

        authService.performTokenRequest(tokenReq) { tr, tex ->
            if (tex != null || tr == null) {
                android.util.Log.e("Auth123", "AUTH: token exchange failed", tex)
                finishWithCanceled()
                return@performTokenRequest
            }

            android.util.Log.d("Auth123", "AUTH: token OK, exp=${tr.accessTokenExpirationTime}")

            val payload = """{
          "access_token":"${tr.accessToken}",
          "refresh_token":"${tr.refreshToken ?: ""}",
          "expires_at":${(tr.accessTokenExpirationTime ?: 0L) / 1000},
          "scope":"${tr.scope ?: ""}"
        }""".trimIndent()

            val handoff = Intent(this@AuthActivity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("group", state)
                putExtra("credentialJson", payload)
            }
            android.util.Log.d("Auth123", "AUTH: handoff -> Main group=$state")
            startActivity(handoff)
            finish()
        }
    }

    @Serializable
    private data class TokenJson(
        @SerialName("access_token") val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String? = null,
        @SerialName("expires_in")   val expiresIn: Long? = null,
        val scope: String? = null,
        @SerialName("id_token")     val idToken: String? = null,
        @SerialName("token_type")   val tokenType: String? = null
    )

//    private suspend fun exchangeCodeForTokens(code: String, verifier: String): TokenJson {
//        val resp: HttpResponse = http.submitForm(
//            url = tokenEndpoint,
//            formParameters = parameters {
//                append("grant_type", "authorization_code")
//                append("code", code)
//                append("client_id", clientId)
//                append("redirect_uri", redirectUri)
//                append("code_verifier", verifier)
//            }
//        )
//        val body = resp.bodyAsText()
//        android.util.Log.d("Auth123", "AUTH: token HTTP ${resp.status} body=$body")
//        if (!resp.status.isSuccess()) {
//            throw IllegalStateException("Token HTTP ${resp.status}: $body")
//        }
//        return Json { ignoreUnknownKeys = true }.decodeFromString(TokenJson.serializer(), body)
//    }


    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun finishWithCanceled() {
        setResult(RESULT_CANCELED); finish()
    }
}