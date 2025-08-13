package com.example.integrationauthenticationplatform

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.room.Room
import com.example.integrationauthenticationplatform.data.AppDb
import com.example.integrationauthenticationplatform.data.CredentialRepo
import com.example.integrationauthenticationplatform.model.ProviderGroup
import com.example.integrationauthenticationplatform.model.ServiceDef
import com.example.integrationauthenticationplatform.oauth.OAuthConfigs
import com.example.integrationauthenticationplatform.ui.DashboardScreen
import com.example.integrationauthenticationplatform.ui.DashboardViewModel

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDb::class.java, "iap.db").build()
    }
    private val repo by lazy { CredentialRepo(db.dao()) }

    private val vm: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(repo)
    }

    // Single unified launcher: prefers per-service result, falls back to group
    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        android.util.Log.d("Auth123", "result=${res.resultCode} extras=${res.data?.extras}")
        if (res.resultCode != RESULT_OK) return@registerForActivityResult

        val data = res.data ?: return@registerForActivityResult
        val credJson = data.getStringExtra("credentialJson") ?: return@registerForActivityResult

        val serviceId = data.getStringExtra("serviceId")
        if (!serviceId.isNullOrBlank()) {
            vm.onOAuthSuccessForService(serviceId, credJson)
            return@registerForActivityResult
        }

        // legacy/group path (Google/Microsoft)
        val groupName = data.getStringExtra("group") ?: return@registerForActivityResult
        val group = runCatching { ProviderGroup.valueOf(groupName) }.getOrNull() ?: return@registerForActivityResult
        vm.onOAuthSuccess(group, credJson)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // still handle old flow if Activity was resumed with extras (safe no-op otherwise)
        consumeOAuthIntent()

        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                DashboardScreen(
                    vm = vm,
                    onRequestOAuth = ::handleOAuthRequest
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        consumeOAuthIntent()
    }

    private fun consumeOAuthIntent() {
        val data = intent ?: return
        val credJson  = data.getStringExtra("credentialJson") ?: return
        val groupName = data.getStringExtra("group") ?: return
        val group = runCatching { ProviderGroup.valueOf(groupName) }.getOrNull() ?: return

        android.util.Log.d("Auth123", "Main received group=$groupName, updating UI")
        vm.onOAuthSuccess(group, credJson)

        // clear so we don’t process again next resume
        setIntent(Intent(this, javaClass))
    }

    private fun handleOAuthRequest(group: ProviderGroup, def: ServiceDef) {
        val cfg = when (def.id) {
            "x"         -> OAuthConfigs.x
            "linkedin"  -> OAuthConfigs.demo
            "instagram" -> OAuthConfigs.demo
            "tiktok"    -> OAuthConfigs.demo
            "facebook"  -> OAuthConfigs.demo
            "snapchat"  -> OAuthConfigs.demo
            else -> when (group) {
                ProviderGroup.Google    -> OAuthConfigs.google
                ProviderGroup.Microsoft -> OAuthConfigs.microsoft
                else -> OAuthConfigs.demo
            }
        }
        val intent = Intent(this, AuthActivity::class.java).apply {
            putExtra("serviceId", def.id)
            putExtra("extraParams", HashMap(cfg.extraParams))
            putExtra("group", cfg.group.name)
            putExtra("clientId", cfg.clientId)
            putExtra("redirectUri", cfg.redirectUri)
            putExtra("authEndpoint", cfg.authEndpoint)
            putExtra("tokenEndpoint", cfg.tokenEndpoint)
            putExtra("scopes", cfg.scopes.joinToString(" "))
        }
        authLauncher.launch(intent)
    }

}
