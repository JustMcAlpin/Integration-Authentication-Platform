package com.example.integrationauthenticationplatform.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.integrationauthenticationplatform.data.CredentialRepo
import com.example.integrationauthenticationplatform.data.RevokeClient
import com.example.integrationauthenticationplatform.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServiceUi(val def: ServiceDef, val connected: Boolean)

class DashboardViewModel(private val repo: CredentialRepo) : ViewModel() {

    private val _services = MutableStateFlow<List<ServiceUi>>(emptyList())
    val services: StateFlow<List<ServiceUi>> = _services.asStateFlow()

    init {
        // Start disconnected; you can hydrate from DB later if you like.
        _services.value = SERVICES.map { ServiceUi(it, connected = false) }
        viewModelScope.launch { refreshFromDb() }
    }

    fun disconnect(service: ServiceDef) {
        viewModelScope.launch {
            // best-effort revoke for Google
            if (service.group == ProviderGroup.Google) {
                repo.getDecrypted(service.displayName)?.let { json ->
                    val refresh = Regex("\"refresh_token\"\\s*:\\s*\"([^\"]*)\"").find(json)?.groupValues?.get(1)
                    val access  = Regex("\"access_token\"\\s*:\\s*\"([^\"]*)\"").find(json)?.groupValues?.get(1)
                    val token = refresh?.takeIf { it.isNotBlank() } ?: access
                    if (token != null) RevokeClient.revokeGoogle(token)
                }
            }
            repo.remove(service.displayName)
            _services.value = _services.value.map {
                if (it.def.id == service.id) it.copy(connected = false) else it
            }
        }
    }


    private suspend fun refreshFromDb() {
        val existing = repo.all().associateBy { it.service }
        _services.value = com.example.integrationauthenticationplatform.model.SERVICES.map { s ->
            ServiceUi(s, connected = existing.containsKey(s.displayName))
        }
    }

    fun saveApiKey(service: ServiceDef, apiKey: String) {
        viewModelScope.launch {
            val json = """{"api_key":"$apiKey"}"""
            repo.save(service.displayName, "api_key", json)
            setConnected(serviceIds = listOf(service.id), connected = true)
        }
    }

    // Call this after a successful OAuth exchange.
    fun onOAuthSuccess(group: ProviderGroup, credentialJson: String) {
        viewModelScope.launch {
            val affected = SERVICES.filter { it.group == group }
            affected.forEach { repo.save(it.displayName, "oauth", credentialJson) }
            setConnected(serviceIds = affected.map { it.id }, connected = true)
        }
    }

    private fun setConnected(serviceIds: List<String>, connected: Boolean) {
        _services.value = _services.value.map {
            if (serviceIds.contains(it.def.id)) it.copy(connected = connected) else it
        }
    }

    class Factory(private val repo: CredentialRepo) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repo) as T
        }
    }
}
