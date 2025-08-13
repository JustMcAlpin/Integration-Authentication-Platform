package com.example.integrationauthenticationplatform

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import com.example.integrationauthenticationplatform.oauth.LoopbackReceiver
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

    private lateinit var serviceId: String
    private lateinit var authService: AuthorizationService
    private val prefs by lazy { getSharedPreferences("auth_pkce", MODE_PRIVATE) }

    // LinkedIn loopback support
    private var loopback: LoopbackReceiver? = null
    private var pendingState: String = ""

    private val isLinkedIn: Boolean
        get() = serviceId == "linkedin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { Surface { CircularProgressIndicator() } }

        serviceId   = intent.getStringExtra("serviceId") ?: ""
        authService = AuthorizationService(this)

        // For non-loopback providers, handle browser redirect intents
        if (!isLinkedIn && (intent?.action == Intent.ACTION_VIEW || intent?.data != null)) {
            handleRedirectFromBrowser(intent)
            return
        }

        // Config from Main
        groupName     = intent.getStringExtra("group")!!
        clientId      = intent.getStringExtra("clientId")!!
        redirectUri   = intent.getStringExtra("redirectUri")!!
        authEndpoint  = intent.getStringExtra("authEndpoint")!!
        tokenEndpoint = intent.getStringExtra("tokenEndpoint")!!
        scopes        = intent.getStringExtra("scopes")!!.split(" ")

        startAuth()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (!isLinkedIn && (intent.action == Intent.ACTION_VIEW || intent.data != null)) {
            handleRedirectFromBrowser(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { loopback?.stop() } catch (_: Exception) {}
        if (this::authService.isInitialized) authService.dispose()
    }

    private fun startAuth() {
        val useLinkedIn = serviceId == "linkedin"

        // DEMO PATH: no client id + demo mode -> generate fake token and return success
        if (BuildConfig.DEMO_MODE && clientId.isBlank()) {
            val now = System.currentTimeMillis() / 1000
            val fakeAccess = "demo_${serviceId}_" + java.util.UUID.randomUUID().toString().replace("-", "")
            val payload = """
            {
              "access_token":"$fakeAccess",
              "refresh_token":"",
              "expires_at":${now + 3600},
              "scope":"basic"
            }
        """.trimIndent()

            val result = Intent().apply {
                putExtra("serviceId", serviceId)
                putExtra("group", groupName)
                putExtra("credentialJson", payload)
            }
            setResult(RESULT_OK, result)
            finish()
            return
        }

        // Real OAuth (Google/Microsoft/X/LinkedIn w/ real client id)

        val serviceCfg = AuthorizationServiceConfiguration(
            Uri.parse(authEndpoint),
            Uri.parse(tokenEndpoint)
        )

        // Safety: don't proceed without a client id (this won't be hit in demo mode)
        if (clientId.isBlank()) {
            android.widget.Toast.makeText(this, "Missing Client ID.", android.widget.Toast.LENGTH_LONG).show()
            finishWithCanceled()
            return
        }

        val codeVerifier = generateCodeVerifier()
        val state = if (serviceId.isNotBlank()) serviceId else groupName
        pendingState = state
        prefs.edit().putString("pkce_verifier_$state", codeVerifier).apply()

        @Suppress("UNCHECKED_CAST")
        val extras = (intent.getSerializableExtra("extraParams") as? HashMap<String, String>)
            ?.toMutableMap() ?: mutableMapOf()
        val prompt = extras.remove("prompt")

        val effectiveRedirect: String = if (useLinkedIn) {
            // If you pasted the NanoHTTPD loopback, enable it; otherwise you can leave LinkedIn in demo.
            loopback = com.example.integrationauthenticationplatform.oauth.LoopbackReceiver { code, _ ->
                onLoopbackCode(serviceCfg, codeVerifier, code)
            }
            loopback!!.start()
            loopback!!.redirectUri
        } else {
            redirectUri
        }

        val builder = AuthorizationRequest.Builder(
            serviceCfg,
            clientId,
            ResponseTypeValues.CODE,
            Uri.parse(effectiveRedirect)
        ).setScopes(scopes)

        if (!prompt.isNullOrBlank()) builder.setPromptValues(prompt)
        builder.setState(state)
        builder.setCodeVerifier(codeVerifier)

        val req = builder.setAdditionalParameters(extras).build()
        startActivity(authService.getAuthorizationRequestIntent(req))
    }


    /** LinkedIn: receive the code via localhost and exchange for tokens */
    private fun onLoopbackCode(
        serviceCfg: AuthorizationServiceConfiguration,
        verifier: String,
        code: String
    ) {
        val stored = prefs.getString("pkce_verifier_$pendingState", null)
        if (stored.isNullOrBlank()) { finishWithCanceled(); return }

        val tokenReq = TokenRequest.Builder(serviceCfg, clientId)
            .setAuthorizationCode(code)
            .setRedirectUri(Uri.parse(loopback!!.redirectUri))
            .setCodeVerifier(stored)
            .build()

        authService.performTokenRequest(tokenReq) { tr, tex ->
            if (tex != null || tr == null) { finishWithCanceled(); return@performTokenRequest }
            val payload = """
            {
              "access_token":"${tr.accessToken}",
              "refresh_token":"${tr.refreshToken ?: ""}",
              "expires_at":${(tr.accessTokenExpirationTime ?: 0L) / 1000},
              "scope":"${tr.scope ?: ""}"
            }
        """.trimIndent()

            val result = Intent().apply {
                putExtra("serviceId", serviceId)
                putExtra("group", groupName)
                putExtra("credentialJson", payload)
            }
            setResult(RESULT_OK, result)
            finish()
        }
    }


    /** Google/Microsoft/X: handle custom-scheme redirect */
    private fun handleRedirectFromBrowser(intent: Intent) {
        val uri = intent.data
        if (uri == null) { finishWithCanceled(); return }

        val code  = uri.getQueryParameter("code") ?: run { finishWithCanceled(); return }
        val state = uri.getQueryParameter("state") ?: (if (serviceId.isNotBlank()) serviceId else groupName)

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

            val payload = """
                {
                  "access_token":"${tr.accessToken}",
                  "refresh_token":"${tr.refreshToken ?: ""}",
                  "expires_at":${(tr.accessTokenExpirationTime ?: 0L) / 1000},
                  "scope":"${tr.scope ?: ""}"
                }
            """.trimIndent()

            val result = Intent().apply {
                putExtra("serviceId", serviceId)   // if provided
                putExtra("group", groupName)
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
