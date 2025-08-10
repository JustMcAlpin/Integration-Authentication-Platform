package com.example.integrationauthenticationplatform.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CredentialEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): CredentialDao
}
