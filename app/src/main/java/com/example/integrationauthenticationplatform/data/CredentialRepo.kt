package com.example.integrationauthenticationplatform.data

import com.example.integrationauthenticationplatform.crypto.Crypto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CredentialRepo(private val dao: CredentialDao) {

    suspend fun save(service: String, authType: String, credentialJson: String) {
        val box = Crypto.encryptJson(credentialJson)
        dao.insert(
            CredentialEntity(
                service = service,
                authType = authType,
                encryptedData = box.cipherB64,
                iv = box.ivB64,
                status = "connected"
            )
        )
    }

    suspend fun remove(service: String) = dao.deleteByService(service)

    // 🔹 used by DashboardViewModel.refreshFromDb()
    suspend fun all(): List<CredentialEntity> = dao.all()

    // 🔹 used by DashboardViewModel.disconnect() for revoke
    suspend fun getDecrypted(service: String): String? {
        val e = dao.get(service) ?: return null
        return Crypto.decryptToJson(e.encryptedData, e.iv)
    }
}
