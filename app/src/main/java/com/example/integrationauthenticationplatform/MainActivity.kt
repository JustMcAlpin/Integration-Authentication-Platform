// MainActivity.kt
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
import com.example.integrationauthenticationplatform.ui.DashboardScreen
import com.example.integrationauthenticationplatform.ui.DashboardViewModel
import com.example.integrationauthenticationplatform.ui.oauth.OAuthConfigs

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDb::class.java, "iap.db").build()
    }
    private val repo by lazy { CredentialRepo(db.dao()) }

    private val vm: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(repo)
    }

    // handles result from AuthActivity (browser redirect comes back to it)
    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == RESULT_OK) {
            val groupName = res.data?.getStringExtra("group") ?: return@registerForActivityResult
            val credJson  = res.data?.getStringExtra("credentialJson") ?: return@registerForActivityResult
            vm.onOAuthSuccess(ProviderGroup.valueOf(groupName), credJson)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                DashboardScreen(
                    vm = vm,
                    onRequestOAuth = ::handleOAuthRequest
                )
            }
        }
    }

    private fun handleOAuthRequest(group: ProviderGroup, @Suppress("UNUSED_PARAMETER") def: ServiceDef) {
        val cfg = when (group) {
            ProviderGroup.Google -> OAuthConfigs.google
            ProviderGroup.Microsoft -> OAuthConfigs.microsoft
            else -> return // socials are marked "Needs approval"
        }
        val intent = Intent(this, AuthActivity::class.java).apply {
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
