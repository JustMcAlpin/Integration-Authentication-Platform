package com.example.integrationauthenticationplatform.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.integrationauthenticationplatform.model.AuthType
import com.example.integrationauthenticationplatform.model.ProviderGroup
import com.example.integrationauthenticationplatform.model.ServiceDef
import com.example.integrationauthenticationplatform.ui.components.ApiKeyDialog
import com.example.integrationauthenticationplatform.ui.components.IntegrationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    onRequestOAuth: (ProviderGroup, ServiceDef) -> Unit, // Activity will start OAuth; call vm.onOAuthSuccess on return
) {
    val items by vm.services.collectAsState()
    var apiKeyTarget by remember { mutableStateOf<ServiceDef?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Integration Authentication Platform") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(220.dp),
            modifier = Modifier
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { ui ->
                IntegrationCard(
                    title = ui.def.displayName,
                    authLabel = if (ui.def.authType == AuthType.OAuth) "OAuth 2.0" else "API Key",
                    icon = ui.def.icon,
                    connected = ui.connected,
                    requiresApproval = ui.def.requiresApproval,
                    onConnect = {
                        when (ui.def.authType) {
                            AuthType.ApiKey -> apiKeyTarget = ui.def
                            AuthType.OAuth -> onRequestOAuth(ui.def.group, ui.def)
                        }
                    },
                    onDisconnect = { vm.disconnect(ui.def) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    apiKeyTarget?.let { target ->
        ApiKeyDialog(
            serviceName = target.displayName,
            onDismiss = { apiKeyTarget = null },
            onSubmit = { key ->
                vm.saveApiKey(target, key)
                apiKeyTarget = null
            }
        )
    }
}
