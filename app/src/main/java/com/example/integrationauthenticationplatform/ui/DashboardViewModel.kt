package com.example.integrationauthenticationplatform.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.integrationauthenticationplatform.BuildConfig
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
        _services.value = SERVICES.map { ServiceUi(it, connected = false) }
        viewModelScope.launch { refreshFromDb() }
    }

    fun disconnect(service: ServiceDef) {
        viewModelScope.launch {
            // only revoke Google if no other Google service is still connected
            val othersUsingGroup = _services.value.any {
                it.def.group == service.group && it.connected && it.def.id != service.id
            }
            if (!othersUsingGroup && service.group == ProviderGroup.Google) {
                repo.getDecrypted(service.displayName)?.let { json ->
                    val refresh = Regex("\"refresh_token\"\\s*:\\s*\"([^\"]*)\"")
                        .find(json)?.groupValues?.getOrNull(1)
                    val access = Regex("\"access_token\"\\s*:\\s*\"([^\"]*)\"")
                        .find(json)?.groupValues?.getOrNull(1)
                    val token = refresh?.takeIf { it.isNotBlank() } ?: access
                    if (!token.isNullOrBlank()) RevokeClient.revokeGoogle(token)
                }
            }

            // X (Twitter): revoke best-effort if we have a token and client id
            if (service.id == "x") {
                repo.getDecrypted(service.displayName)?.let { json ->
                    val refresh = Regex("\"refresh_token\"\\s*:\\s*\"([^\"]*)\"")
                        .find(json)?.groupValues?.getOrNull(1)
                    val access = Regex("\"access_token\"\\s*:\\s*\"([^\"]*)\"")
                        .find(json)?.groupValues?.getOrNull(1)
                    val tokenToRevoke = refresh?.takeIf { it.isNotBlank() } ?: access
                    val clientId = BuildConfig.TWITTER_CLIENT_ID
                    if (!tokenToRevoke.isNullOrBlank() && clientId.isNotBlank()) {
                        RevokeClient.revokeX(tokenToRevoke, clientId)
                    }
                }
            }

            repo.remove(service.displayName)
            _services.value = _services.value.map {
                if (it.def.id == service.id) it.copy(connected = false) else it
            }
        }
    }

    // Generic single-key save (fallback)
    fun saveApiKey(service: ServiceDef, apiKey: String) {
        viewModelScope.launch {
            val json = """{"api_key":"$apiKey"}"""
            repo.save(service.displayName, "api_key", json)
            setConnected(listOf(service.id), true)
        }
    }

    // SendGrid: validate then store
    fun connectSendGrid(service: ServiceDef, apiKey: String) {
        viewModelScope.launch {
            val ok = SendGridClient.validate(apiKey)
            if (ok) {
                val json = """{"api_key":"$apiKey"}"""
                repo.save(service.displayName, "api_key", json)
                setConnected(listOf(service.id), true)
            }
        }
    }

    // Twilio: Account SID + Auth Token
    fun connectTwilio(service: ServiceDef, accountSid: String, authToken: String) {
        viewModelScope.launch {
            val ok = TwilioClient.validate(accountSid, authToken)
            if (ok) {
                val json = """{"account_sid":"$accountSid","auth_token":"$authToken"}"""
                repo.save(service.displayName, "api_key", json)
                setConnected(listOf(service.id), true)
            }
        }
    }

    private suspend fun refreshFromDb() {
        val existing = repo.all().associateBy { it.service }
        _services.value = SERVICES.map { s ->
            ServiceUi(s, connected = existing.containsKey(s.displayName))
        }
    }

    fun onOAuthSuccessForService(serviceId: String, credentialJson: String) {
        viewModelScope.launch {
            val service = SERVICES.firstOrNull { it.id == serviceId } ?: return@launch
            repo.save(service.displayName, "oauth", credentialJson)
            setConnected(listOf(service.id), true)
        }
    }

    // Called after OAuth success to fan out to group services
    fun onOAuthSuccess(group: ProviderGroup, credentialJson: String) {
        viewModelScope.launch {
            val affected = SERVICES.filter { it.group == group }
            affected.forEach { repo.save(it.displayName, "oauth", credentialJson) }
            setConnected(affected.map { it.id }, true)
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
