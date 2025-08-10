package com.example.integrationauthenticationplatform.data

import androidx.room.*

@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val service: String,
    val authType: String,          // "oauth" | "api_key"
    val encryptedData: String,     // base64 ciphertext
    val iv: String,                // base64 IV
    val status: String,            // "connected" | "disconnected"
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface CredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(e: CredentialEntity): Long
    @Query("DELETE FROM credentials WHERE service = :service") suspend fun deleteByService(service: String)
    @Query("SELECT * FROM credentials WHERE service = :service LIMIT 1") suspend fun get(service: String): CredentialEntity?
    @Query("SELECT * FROM credentials") suspend fun all(): List<CredentialEntity>
}
