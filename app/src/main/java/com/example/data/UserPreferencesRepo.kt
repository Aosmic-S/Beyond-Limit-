package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepo(private val context: Context) {
    companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val BLF_BACKUP_KEY = stringPreferencesKey("blf_backup_content")
        val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
    }

    val userNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }
    
    val blfBackupFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[BLF_BACKUP_KEY]
    }
    
    val openRouterKeyFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[OPENROUTER_API_KEY]
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }
    
    suspend fun saveBlfBackup(content: String) {
        context.dataStore.edit { preferences ->
            preferences[BLF_BACKUP_KEY] = content
        }
    }
    
    suspend fun saveOpenRouterKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENROUTER_API_KEY] = key
        }
    }
}
